package ge.mysky.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ge.mysky.backend.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class GraniteControllerTest extends AbstractIntegrationTest {

    @Test
    void anyoneCanReadGraniteConfigOnlyAdminCanUpdate() throws Exception {
        var admin = adminToken();

        mvc.perform(get("/granite").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        mvc.perform(put("/granite")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("pricePerMeter", 45, "timePerMeterMinutes", 8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricePerMeter").value(45));
    }

    @Test
    void getRequiresAuth() throws Exception {
        mvc.perform(get("/granite")).andExpect(status().isUnauthorized());
    }

    @Test
    void updateRequiresAuth() throws Exception {
        mvc.perform(put("/granite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("pricePerMeter", 1, "timePerMeterMinutes", 1))))
                .andExpect(status().isUnauthorized());
    }
}
