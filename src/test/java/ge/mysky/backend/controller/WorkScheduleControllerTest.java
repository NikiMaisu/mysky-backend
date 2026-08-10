package ge.mysky.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ge.mysky.backend.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class WorkScheduleControllerTest extends AbstractIntegrationTest {

    @Test
    void adminCanReadAndUpdateGlobalSchedule() throws Exception {
        var admin = adminToken();

        mvc.perform(get("/work-schedule").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.start").exists());

        var days = new boolean[]{true, true, true, true, true, false, false};
        mvc.perform(put("/work-schedule")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("days", days, "start", "08:00", "end", "16:00"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.start").value("08:00"))
                .andExpect(jsonPath("$.end").value("16:00"));
    }

    @Test
    void updateRejectsEndBeforeStart() throws Exception {
        var admin = adminToken();
        var days = new boolean[]{true, false, false, false, false, false, false};

        mvc.perform(put("/work-schedule")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("days", days, "start", "16:00", "end", "08:00"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequiresAuth() throws Exception {
        mvc.perform(get("/work-schedule")).andExpect(status().isUnauthorized());
    }
}
