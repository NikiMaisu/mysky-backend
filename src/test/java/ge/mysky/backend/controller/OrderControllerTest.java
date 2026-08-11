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

    @Test
    void createWithFixturesAndAddonsIncludesThemInTotals() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);

        var fixtureRes = mvc.perform(authed(post("/fixtures"), admin,
                        Map.of("name", "Spotlight-" + sfx, "unit", "PER_UNIT", "cost", 50, "installTimeMinutes", 10)))
                .andExpect(status().isCreated()).andReturn();
        long fixtureId = json.readTree(fixtureRes.getResponse().getContentAsString()).get("id").asLong();

        var addonRes = mvc.perform(authed(post("/addons"), admin,
                        Map.of("name", "Blinds-" + sfx, "category", "BLINDS_RAILING", "cost", 30, "installTimeMinutes", 15)))
                .andExpect(status().isCreated()).andReturn();
        long addonId = json.readTree(addonRes.getResponse().getContentAsString()).get("id").asLong();

        var body = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        body.put("fixtures", java.util.List.of(Map.of("fixtureId", fixtureId, "quantity", 2)));
        body.put("addons", java.util.List.of(Map.of("addonId", addonId, "quantity", 1)));

        mvc.perform(authed(post("/orders"), admin, body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fixtures[0].name").value("Spotlight-" + sfx))
                .andExpect(jsonPath("$.addons[0].name").value("Blinds-" + sfx))
                // 20 (material) + 20 (2x fixture) + 15 (1x addon)
                .andExpect(jsonPath("$.totalMinutes").value(55));
    }

    @Test
    void createWithCustomPriceOverridesCalculatedCost() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);

        var body = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        body.put("costOverridden", true);
        body.put("totalCost", 999);

        mvc.perform(authed(post("/orders"), admin, body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costOverridden").value(true))
                .andExpect(jsonPath("$.totalCost").value(999));
    }

    @Test
    void createWithManualOrderNumberRejectsDuplicate() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);
        long manualNumber = 900000L + Long.parseLong(sfx.substring(sfx.length() - 5));

        var body = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        body.put("orderNumber", manualNumber);

        mvc.perform(authed(post("/orders"), admin, body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value(manualNumber));

        mvc.perform(authed(post("/orders"), admin, body))
                .andExpect(status().isConflict());
    }

    @Test
    void listSupportsSearchStatusAndTeamFilters() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);
        var client = "Findme-" + sfx;

        var body = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        body.put("clientName", client);
        var createRes = mvc.perform(authed(post("/orders"), admin, body))
                .andExpect(status().isCreated()).andReturn();
        long id = json.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/orders?q=" + client).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(get("/orders?status=QUOTED&q=" + client).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(get("/orders?status=DONE&q=" + client).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void workersOnlySeeOrdersForTheirOwnTeam() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);

        long teamId = json.readTree(mvc.perform(authed(post("/teams"), admin,
                        Map.of("name", "OrderTeam-" + sfx, "memberIds", java.util.List.of())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();

        var workerEmail = "orderworker-" + sfx + "@mysky.ge";
        long workerId = json.readTree(mvc.perform(authed(post("/workers"), admin,
                        Map.of("name", "OrderWorker-" + sfx, "email", workerEmail, "password", "password1")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(authed(post("/teams/" + teamId + "/members"), admin, Map.of("workerId", workerId)))
                .andExpect(status().isOk());

        var body = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        body.put("teamId", teamId);
        body.put("clientName", "Scoped-" + sfx);
        mvc.perform(authed(post("/orders"), admin, body))
                .andExpect(status().isCreated());

        var workerToken = tokenFor(workerEmail, "password1");
        mvc.perform(get("/orders?q=Scoped-" + sfx).header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientName").value("Scoped-" + sfx));

        var otherWorkerEmail = "otherworker-" + sfx + "@mysky.ge";
        mvc.perform(authed(post("/workers"), admin,
                        Map.of("name", "OtherWorker-" + sfx, "email", otherWorkerEmail, "password", "password1")))
                .andExpect(status().isCreated());
        var otherWorkerToken = tokenFor(otherWorkerEmail, "password1");

        mvc.perform(get("/orders?q=Scoped-" + sfx).header("Authorization", "Bearer " + otherWorkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void adminCanExportOrdersAsCsvAndXlsx() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);
        mvc.perform(authed(post("/orders"), admin, orderBody(materialId, 4)))
                .andExpect(status().isCreated());

        mvc.perform(get("/orders/export?format=csv").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", org.hamcrest.Matchers.containsString(".csv")));

        mvc.perform(get("/orders/export?format=xlsx").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", org.hamcrest.Matchers.containsString(".xlsx")));
    }

    @Test
    void exportRequiresAdminRole() throws Exception {
        var admin = adminToken();
        mvc.perform(authed(post("/workers"), admin,
                        Map.of("name", "ExportW-" + sfx, "email", "exportw-" + sfx + "@mysky.ge", "password", "password1")))
                .andExpect(status().isCreated());
        var workerToken = tokenFor("exportw-" + sfx + "@mysky.ge", "password1");

        mvc.perform(get("/orders/export").header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void manualFinishOverrideSkipsScheduleCalculation() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);

        var body = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        body.put("finishOverridden", true);
        body.put("finishAt", "2099-05-06T18:00:00+04:00");

        mvc.perform(authed(post("/orders"), admin, body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.finishOverridden").value(true))
                .andExpect(jsonPath("$.finishAt").value(org.hamcrest.Matchers.startsWith("2099-05-06T14:00")));
    }

    @Test
    void costOverriddenWithoutTotalCostFallsBackToCalculatedCost() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);

        var body = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        body.put("costOverridden", true);
        // totalCost intentionally omitted

        mvc.perform(authed(post("/orders"), admin, body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costOverridden").value(false))
                .andExpect(jsonPath("$.totalCost").value(40));
    }

    @Test
    void flatAddedValueSupportsHoursAndDaysUnits() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);

        var hoursBody = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        hoursBody.put("flatAddedValue", 2);
        hoursBody.put("flatAddedUnit", "HOURS");
        // 20 (material) + 120 (2h flat) = 140
        mvc.perform(authed(post("/orders"), admin, hoursBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalMinutes").value(140));

        var daysBody = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        daysBody.put("flatAddedValue", 1);
        daysBody.put("flatAddedUnit", "DAYS");
        mvc.perform(authed(post("/orders"), admin, daysBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalMinutes").isNumber());
    }

    @Test
    void createRejectsUnknownTeamMaterialFixtureOrAddon() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);

        var badTeam = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        badTeam.put("teamId", 999999L);
        mvc.perform(authed(post("/orders"), admin, badTeam))
                .andExpect(status().isNotFound());

        var badMaterial = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        badMaterial.put("materials", java.util.List.of(Map.of("materialId", 999999L, "squareMeters", 4)));
        mvc.perform(authed(post("/orders"), admin, badMaterial))
                .andExpect(status().isNotFound());

        var badFixture = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        badFixture.put("fixtures", java.util.List.of(Map.of("fixtureId", 999999L, "quantity", 1)));
        mvc.perform(authed(post("/orders"), admin, badFixture))
                .andExpect(status().isNotFound());

        var badAddon = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        badAddon.put("addons", java.util.List.of(Map.of("addonId", 999999L, "quantity", 1)));
        mvc.perform(authed(post("/orders"), admin, badAddon))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAcceptsExplicitStatusAndBlanksOptionalFields() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);

        var body = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        body.put("status", "SCHEDULED");
        body.put("clientPhone", "");
        body.put("address", "");
        body.put("notes", "");

        var res = mvc.perform(authed(post("/orders"), admin, body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andReturn();
        var order = json.readTree(res.getResponse().getContentAsString());
        assertThatFieldIsAbsentOrNull(order, "clientPhone");
        assertThatFieldIsAbsentOrNull(order, "address");
        assertThatFieldIsAbsentOrNull(order, "notes");
    }

    private void assertThatFieldIsAbsentOrNull(com.fasterxml.jackson.databind.JsonNode node, String field) {
        org.assertj.core.api.Assertions.assertThat(node.get(field) == null || node.get(field).isNull()).isTrue();
    }

    @Test
    void updateCanChangeOrderNumberToAnUnusedOne() throws Exception {
        var admin = adminToken();
        var materialId = createMaterial(admin);
        long newNumber = 800000L + Long.parseLong(sfx.substring(sfx.length() - 5));

        var createRes = mvc.perform(authed(post("/orders"), admin, orderBody(materialId, 4)))
                .andExpect(status().isCreated()).andReturn();
        long id = json.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        var body = new java.util.HashMap<String, Object>(orderBody(materialId, 4));
        body.put("orderNumber", newNumber);

        mvc.perform(authed(put("/orders/" + id), admin, body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(newNumber));
    }
}
