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

class AddonControllerTest extends AbstractIntegrationTest {

    private final String sfx = Long.toString(System.nanoTime());

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder b, String token, Object body) throws Exception {
        b.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
        if (body != null) b.content(json.writeValueAsString(body));
        return b;
    }

    @Test
    void adminCanCreateListGetUpdateAndDeleteAddon() throws Exception {
        var admin = adminToken();
        var name = "Blinds-" + sfx;

        var createRes = mvc.perform(authed(post("/addons"), admin,
                        Map.of("name", name, "category", "BLINDS_RAILING", "cost", 30, "installTimeMinutes", 15)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn();
        long id = json.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/addons?category=BLINDS_RAILING").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        mvc.perform(get("/addons/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("BLINDS_RAILING"));

        mvc.perform(authed(put("/addons/" + id), admin,
                        Map.of("name", name + "-updated", "category", "HVAC_CUTOUT", "cost", 40, "installTimeMinutes", 20)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("HVAC_CUTOUT"));

        mvc.perform(authed(delete("/addons/" + id), admin, null))
                .andExpect(status().isNoContent());
    }

    @Test
    void createRequiresAuth() throws Exception {
        mvc.perform(post("/addons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("name", "x", "cost", 1, "installTimeMinutes", 1))))
                .andExpect(status().isUnauthorized());
    }
}
