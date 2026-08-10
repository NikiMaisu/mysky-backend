package ge.mysky.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import ge.mysky.backend.domain.OrderStatus;
import ge.mysky.backend.dto.OrderResponse;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ExportServiceTest {

    private final ExportService service = new ExportService();

    private static OrderResponse basicOrder(String clientName, String notes) {
        var material = new OrderResponse.MaterialLine(
                1L, "Matte white", BigDecimal.valueOf(25), BigDecimal.valueOf(12),
                BigDecimal.valueOf(10), BigDecimal.valueOf(250), BigDecimal.valueOf(120));
        var fixture = new OrderResponse.FixtureLine(
                2L, "Spotlight", ge.mysky.backend.domain.FixtureUnit.PER_UNIT,
                BigDecimal.valueOf(50), BigDecimal.valueOf(10), BigDecimal.valueOf(4),
                BigDecimal.valueOf(200), BigDecimal.valueOf(40));
        var addon = new OrderResponse.AddonLine(
                3L, "Blinds", ge.mysky.backend.domain.AddonCategory.BLINDS_RAILING,
                BigDecimal.valueOf(30), 15, 2, BigDecimal.valueOf(60), 30);

        return new OrderResponse(
                1L, 1001L, clientName, "599112233", "12 Rustaveli Ave",
                OffsetDateTime.of(2026, 6, 8, 10, 0, 0, 0, ZoneOffset.ofHours(4)),
                OffsetDateTime.of(2026, 6, 8, 14, 0, 0, 0, ZoneOffset.ofHours(4)),
                1L, "Team A", false, null, null, null,
                0, 190, BigDecimal.valueOf(510), false, false,
                OrderStatus.SCHEDULED, notes,
                List.of(material), List.of(fixture), List.of(addon));
    }

    @Test
    void csvHasHeaderAndOneRowPerOrder() {
        var csv = new String(service.csv(List.of(basicOrder("Ana", "call before arriving"))), StandardCharsets.UTF_8);
        var lines = csv.replace("﻿", "").split("\r\n");

        assertThat(lines).hasSize(2);
        assertThat(lines[0]).contains("Order #", "Status", "Client");
        assertThat(lines[1]).contains("\"1001\"", "\"SCHEDULED\"", "\"Ana\"");
    }

    @Test
    void csvEscapesQuotesAndCommasInFields() {
        var csv = new String(service.csv(List.of(basicOrder("Giorgi \"G\" Beridze, Jr.", null))), StandardCharsets.UTF_8);
        var lines = csv.replace("﻿", "").split("\r\n");

        assertThat(lines[1]).contains("\"Giorgi \"\"G\"\" Beridze, Jr.\"");
    }

    @Test
    void csvHandlesEmptyOrderList() {
        var csv = new String(service.csv(List.of()), StandardCharsets.UTF_8);
        var lines = csv.replace("﻿", "").split("\r\n");

        assertThat(lines).hasSize(1);
    }

    @Test
    void csvRendersNullFieldsAsEmpty() {
        var order = basicOrder("Ana", null);
        var csv = new String(service.csv(List.of(order)), StandardCharsets.UTF_8);

        assertThat(csv).doesNotContain("null");
    }

    @Test
    void xlsxContainsHeaderAndOrderRow() throws Exception {
        var bytes = service.xlsx(List.of(basicOrder("Ana", "notes here")));

        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = wb.getSheetAt(0);
            var header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Order #");

            var row = sheet.getRow(1);
            assertThat(row.getCell(0).getNumericCellValue()).isEqualTo(1001.0);
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Ana");
        }
    }

    @Test
    void xlsxLeavesNullFieldsBlank() throws Exception {
        var order = basicOrder("Ana", null);
        var bytes = service.xlsx(List.of(order));

        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var row = wb.getSheetAt(0).getRow(1);
            assertThat(row.getCell(17).getCellType().toString()).isEqualTo("BLANK");
        }
    }
}
