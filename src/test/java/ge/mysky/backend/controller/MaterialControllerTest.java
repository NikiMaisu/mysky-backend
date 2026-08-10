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

class MaterialControllerTest extends AbstractIntegrationTest {

    private final String sfx = Long.toString(System.nanoTime());

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder b, String token, Object body) throws Exception {
        b.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
        if (body != null) b.content(json.writeValueAsString(body));
        return b;
    }

    @Test
    void adminCanCreateListGetUpdateAndDeleteMaterial() throws Exception {
        var admin = adminToken();
        var name = "Matte-" + sfx;

        var createRes = mvc.perform(authed(post("/materials"), admin,
                        Map.of("name", name, "pricePerM2", 25, "timePerM2Minutes", 10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn();
        long id = json.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/materials").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        mvc.perform(get("/materials/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));

        mvc.perform(authed(put("/materials/" + id), admin,
                        Map.of("name", name + "-updated", "pricePerM2", 30, "timePerM2Minutes", 12)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name + "-updated"));

        mvc.perform(authed(delete("/materials/" + id), admin, null))
                .andExpect(status().isNoContent());
    }

    @Test
    void createRequiresAuth() throws Exception {
        mvc.perform(post("/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("name", "x", "pricePerM2", 1, "timePerM2Minutes", 1))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createValidatesRequiredFields() throws Exception {
        var admin = adminToken();

        mvc.perform(authed(post("/materials"), admin, Map.of("name", "", "pricePerM2", 1, "timePerM2Minutes", 1)))
                .andExpect(status().isBadRequest());
    }
}
