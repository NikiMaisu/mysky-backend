package ge.mysky.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ge.mysky.backend.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class FixtureControllerTest extends AbstractIntegrationTest {

    private final String sfx = Long.toString(System.nanoTime());

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder b, String token, Object body) throws Exception {
        b.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
        if (body != null) b.content(json.writeValueAsString(body));
        return b;
    }

    @Test
    void adminCanCreateListGetUpdateAndDeleteFixture() throws Exception {
        var admin = adminToken();
        var name = "Spotlight-" + sfx;

        var createRes = mvc.perform(authed(post("/fixtures"), admin,
                        Map.of("name", name, "unit", "PER_UNIT", "cost", 50, "installTimeMinutes", 10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn();
        long id = json.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/fixtures").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        mvc.perform(get("/fixtures/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unit").value("PER_UNIT"));

        mvc.perform(authed(put("/fixtures/" + id), admin,
                        Map.of("name", name + "-updated", "unit", "PER_METER", "cost", 75, "installTimeMinutes", 20)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unit").value("PER_METER"));

        mvc.perform(authed(delete("/fixtures/" + id), admin, null))
                .andExpect(status().isNoContent());
    }

    @Test
    void createRequiresAuth() throws Exception {
        mvc.perform(post("/fixtures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("name", "x", "unit", "PER_UNIT", "cost", 1, "installTimeMinutes", 1))))
                .andExpect(status().isUnauthorized());
    }
}
