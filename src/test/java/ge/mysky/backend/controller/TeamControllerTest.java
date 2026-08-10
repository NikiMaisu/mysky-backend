package ge.mysky.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ge.mysky.backend.AbstractIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class TeamControllerTest extends AbstractIntegrationTest {

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
    void adminCanManageTeamAndItsMembers() throws Exception {
        var admin = adminToken();
        var teamName = "Team-" + sfx;

        long workerId = postId("/workers", admin, Map.of(
                "name", "Worker-" + sfx, "email", "worker" + sfx + "@mysky.ge", "password", "password1"));

        var createRes = mvc.perform(authed(post("/teams"), admin, Map.of("name", teamName, "memberIds", List.of())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(teamName))
                .andReturn();
        long teamId = json.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/teams").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        mvc.perform(get("/teams/" + teamId).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(teamName));

        mvc.perform(authed(post("/teams/" + teamId + "/members"), admin, Map.of("workerId", workerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].id").value(workerId));

        mvc.perform(authed(delete("/teams/" + teamId + "/members/" + workerId), admin, null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members").isEmpty());

        mvc.perform(authed(put("/teams/" + teamId), admin, Map.of("name", teamName + "-updated", "memberIds", List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(teamName + "-updated"));

        mvc.perform(authed(delete("/teams/" + teamId), admin, null))
                .andExpect(status().isNoContent());
    }

    @Test
    void createRequiresAuth() throws Exception {
        mvc.perform(post("/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("name", "x", "memberIds", List.of()))))
                .andExpect(status().isUnauthorized());
    }
}
