package ge.mysky.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ge.mysky.backend.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthControllerTest extends AbstractIntegrationTest {

    private String login(String email, String pw) throws Exception {
        return json.writeValueAsString(Map.of("identifier", email, "password", pw));
    }

    @Test
    void loginReturnsTokensAndUser() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(login(ADMIN_EMAIL, ADMIN_PW)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void badPasswordIsUnauthorized() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(login(ADMIN_EMAIL, "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meRequiresTokenAndReturnsCurrentUser() throws Exception {
        var token = adminToken();
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ADMIN_EMAIL));

        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshIssuesNewAccessToken() throws Exception {
        var res = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(login(ADMIN_EMAIL, ADMIN_PW)))
                .andExpect(status().isOk())
                .andReturn();
        var refresh = json.readTree(res.getResponse().getContentAsString()).get("refreshToken").asText();

        var body = json.writeValueAsString(Map.of("refreshToken", refresh));
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void usingRefreshTokenAsAccessTokenIsRejected() throws Exception {
        var res = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(login(ADMIN_EMAIL, ADMIN_PW)))
                .andReturn();
        var refresh = json.readTree(res.getResponse().getContentAsString()).get("refreshToken").asText();
        // a refresh token must not authenticate normal requests
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());
        assertThat(refresh).isNotBlank();
    }
}
