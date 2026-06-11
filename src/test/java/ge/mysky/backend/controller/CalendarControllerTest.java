package ge.mysky.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import ge.mysky.backend.AbstractIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class CalendarControllerTest extends AbstractIntegrationTest {

    private final String sfx = Long.toString(System.nanoTime());

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder b, String token, Object body) throws Exception {
        b.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
        if (body != null) b.content(json.writeValueAsString(body));
        return b;
    }

    private long postId(String path, String token, Object body) throws Exception {
        var res = mvc.perform(authed(post(path), token, body)).andExpect(status().isCreated()).andReturn();
        return json.readTree(res.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void calendarReturnsOrdersAndAvailabilityForRange() throws Exception {
        var admin = adminToken();
        var materialId = postId("/materials", admin, Map.of("name", "CalMat-" + sfx, "pricePerM2", 10, "timePerM2Minutes", 5));
        long teamId = postId("/teams", admin, Map.of("name", "CalTeam-" + sfx, "memberIds", List.of()));

        var client = "CalClient-" + sfx;
        postId("/orders", admin, Map.of(
                "clientName", client,
                "startAt", "2099-05-05T10:00:00+04:00",
                "teamId", teamId,
                "materialId", materialId,
                "squareMeters", 4,
                "graniteEnabled", false,
                "fixtures", List.of(),
                "addons", List.of()));

        var from = "2099-05-04T00:00:00+04:00";
        var to = "2099-05-11T00:00:00+04:00";

        // /calendar returns the order in range
        var calRes = mvc.perform(get("/calendar?from=" + from + "&to=" + to).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn();
        var orders = json.readTree(calRes.getResponse().getContentAsString());
        boolean found = false;
        for (JsonNode o : orders) {
            if (client.equals(o.get("clientName").asText())) found = true;
        }
        assertThat(found).as("calendar should contain the created order").isTrue();

        // /calendar/availability: the order's day shows the team busy
        var availRes = mvc.perform(get("/calendar/availability?from=" + from + "&to=" + to).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn();
        var days = json.readTree(availRes.getResponse().getContentAsString());
        assertThat(days).hasSize(7);
        JsonNode busyDay = null;
        for (JsonNode d : days) {
            if ("2099-05-05".equals(d.get("date").asText())) busyDay = d;
        }
        assertThat(busyDay).as("2099-05-05 present").isNotNull();
        boolean teamBusy = false;
        for (JsonNode tm : busyDay.get("busyTeams")) {
            if (tm.get("id").asLong() == teamId) teamBusy = true;
        }
        assertThat(teamBusy).as("team should be busy on the order's day").isTrue();
    }

    @Test
    void calendarRequiresAuth() throws Exception {
        mvc.perform(get("/calendar?from=2099-05-04T00:00:00%2B04:00&to=2099-05-11T00:00:00%2B04:00"))
                .andExpect(status().isUnauthorized());
    }
}
