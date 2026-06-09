package ge.mysky.backend.service;

import ge.mysky.backend.dto.OrderResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExportService {

    private static final DateTimeFormatter DT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Tbilisi"));

    private static final String[] HEADERS = {
        "Order #", "Status", "Client", "Phone", "Address", "Start", "Finish", "Team",
        "Material", "m²", "Granite", "Perimeter", "Flat added (min)", "Total time (min)",
        "Total cost", "Fixtures", "Add-ons", "Notes",
    };

    private Object[] row(OrderResponse o) {
        return new Object[] {
            o.orderNumber(),
            o.status().name(),
            o.clientName(),
            o.clientPhone(),
            o.address(),
            fmt(o.startAt()),
            fmt(o.finishAt()),
            o.teamName(),
            o.materialName(),
            o.squareMeters(),
            o.graniteEnabled() ? "yes" : "no",
            o.perimeter(),
            o.flatAddedMinutes(),
            o.totalMinutes(),
            o.totalCost(),
            o.fixtures().stream().map(f -> f.name() + " ×" + strip(f.quantity().toString())).collect(Collectors.joining("; ")),
            o.addons().stream().map(a -> a.name() + " ×" + a.quantity()).collect(Collectors.joining("; ")),
            o.notes(),
        };
    }

    public byte[] csv(List<OrderResponse> orders) {
        var sb = new StringBuilder("﻿"); // BOM so Excel reads UTF-8 (Georgian) correctly
        sb.append(String.join(",", quoteAll(HEADERS))).append("\r\n");
        for (var o : orders) {
            sb.append(String.join(",", quoteRow(row(o)))).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] xlsx(List<OrderResponse> orders) {
        try (Workbook wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Orders");
            var headerStyle = wb.createCellStyle();
            var bold = wb.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);

            Row header = sheet.createRow(0);
            for (int c = 0; c < HEADERS.length; c++) {
                Cell cell = header.createCell(c);
                cell.setCellValue(HEADERS[c]);
                cell.setCellStyle(headerStyle);
            }

            int r = 1;
            for (var o : orders) {
                Row row = sheet.createRow(r++);
                var values = row(o);
                for (int c = 0; c < values.length; c++) {
                    Cell cell = row.createCell(c);
                    Object v = values[c];
                    if (v == null) {
                        cell.setBlank();
                    } else if (v instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                    } else {
                        cell.setCellValue(v.toString());
                    }
                }
            }
            for (int c = 0; c < HEADERS.length; c++) sheet.autoSizeColumn(c);

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String fmt(OffsetDateTime t) {
        return t == null ? "" : DT.format(t);
    }

    private static String strip(String numeric) {
        return numeric.contains(".") ? numeric.replaceAll("0+$", "").replaceAll("\\.$", "") : numeric;
    }

    private static String[] quoteAll(String[] cells) {
        var out = new String[cells.length];
        for (int i = 0; i < cells.length; i++) out[i] = quote(cells[i]);
        return out;
    }

    private static String[] quoteRow(Object[] cells) {
        var out = new String[cells.length];
        for (int i = 0; i < cells.length; i++) out[i] = quote(cells[i] == null ? "" : cells[i].toString());
        return out;
    }

    private static String quote(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
