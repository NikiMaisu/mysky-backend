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

class OrderControllerTest extends AbstractIntegrationTest {

    private final String sfx = Long.toString(System.nanoTime());

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder b, String token, Object body) throws Exception {
        b.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
        if (body != null) b.content(json.writeValueAsString(body));
        return b;
    }

    private long createMaterial(String token) throws Exception {
        var res = mvc.perform(authed(post("/materials"), token,
                        Map.of("name", "MatTest-" + sfx, "pricePerM2", 10, "timePerM2Minutes", 5)))
                .andExpect(status().isCreated())
                .andReturn();
        return json.readTree(res.getResponse().getContentAsString()).get("id").asLong();
    }

    private Map<String, Object> orderBody(long materialId, int sqm) {
        return Map.of(
                "clientName", "OrderTest-" + sfx,
                "startAt", "2099-05-05T10:00:00+04:00",
                "materials", java.util.List.of(Map.of("materialId", materialId, "squareMeters", sqm)),
                "graniteEnabled", false,
                "fixtures", java.util.List.of(),
                "addons", java.util.List.of());
    }

    @Test
    void fullCrudCycleWithCalculation() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);

        // create: 10/m² price, 5min/m² time, 4 m² -> cost 40, time 20
        var createRes = mvc.perform(authed(post("/orders"), admin, orderBody(materialId, 4)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalMinutes").value(20))
                .andExpect(jsonPath("$.orderNumber").isNumber())
                .andReturn();
        long id = json.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        // read
        mvc.perform(get("/orders/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("OrderTest-" + sfx));

        // update: 10 m² -> time 50
        mvc.perform(authed(put("/orders/" + id), admin, orderBody(materialId, 10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMinutes").value(50));

        // cancel (soft delete)
        mvc.perform(delete("/orders/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/orders/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void authEnforcement() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);

        // unauthenticated -> 401
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(orderBody(materialId, 4))))
                .andExpect(status().isUnauthorized());

        // worker -> 403
        mvc.perform(authed(post("/workers"), admin,
                        Map.of("name", "WT-" + sfx, "email", "wt-" + sfx + "@mysky.ge", "password", "secret123")))
                .andExpect(status().isCreated());
        var workerToken = tokenFor("wt-" + sfx + "@mysky.ge", "secret123");
        mvc.perform(authed(post("/orders"), workerToken, orderBody(materialId, 4)))
                .andExpect(status().isForbidden());

        // worker can read the list
        mvc.perform(get("/orders").header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk());
    }

    @Test
    void graniteWithoutPerimeterIsBadRequest() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);
        var body = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        body.put("graniteEnabled", true);
        mvc.perform(authed(post("/orders"), admin, body))
                .andExpect(status().isBadRequest());
    }
}
