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

class WorkerControllerTest extends AbstractIntegrationTest {

    private final String sfx = Long.toString(System.nanoTime());

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder b, String token, Object body) throws Exception {
        b.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
        if (body != null) b.content(json.writeValueAsString(body));
        return b;
    }

    @Test
    void adminCanCreateListGetUpdateAndDeleteWorker() throws Exception {
        var admin = adminToken();
        var email = "worker" + sfx + "@mysky.ge";

        var createRes = mvc.perform(authed(post("/workers"), admin,
                        Map.of("name", "Worker-" + sfx, "email", email, "password", "password1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andReturn();
        long id = json.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/workers").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        mvc.perform(get("/workers/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        mvc.perform(authed(put("/workers/" + id), admin,
                        Map.of("name", "Worker-" + sfx + "-updated", "email", email, "phone", "")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Worker-" + sfx + "-updated"));

        mvc.perform(authed(delete("/workers/" + id), admin, null))
                .andExpect(status().isNoContent());
    }

    @Test
    void createRejectsDuplicateEmail() throws Exception {
        var admin = adminToken();
        var email = "dup" + sfx + "@mysky.ge";

        mvc.perform(authed(post("/workers"), admin, Map.of("name", "First", "email", email, "password", "password1")))
                .andExpect(status().isCreated());

        mvc.perform(authed(post("/workers"), admin, Map.of("name", "Second", "email", email, "password", "password1")))
                .andExpect(status().isConflict());
    }

    @Test
    void listRequiresAuth() throws Exception {
        mvc.perform(get("/workers")).andExpect(status().isUnauthorized());
    }
}
