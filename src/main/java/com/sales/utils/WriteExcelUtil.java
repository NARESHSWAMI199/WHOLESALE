package com.sales.utils;

import com.sales.admin.repositories.ItemHbRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WriteExcelUtil {

    
    private final Logger logger = LoggerFactory.getLogger(WriteExcelUtil.class);

    @Value("${excel.export.absolute}")
    String excelExportAbsolutePath;

    @Value("${excel.notUpdated.absolute}")
    String getExcelNotUpdateItemsFolderPath;

    public String createExcelSheet(
            Map<String, List<Object>> data,
            int totalRows,
            List<String> headers,
            String folderName) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Items");
            applyDefaultColumnWidths(sheet);

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            populateHeaders(headerRow, headers, headerStyle);

            CellStyle dataStyle = createDataStyle(workbook);
            populateDataRows(sheet, data, headers, totalRows, dataStyle);

            return saveWorkbook(workbook, folderName);
        }
    }


    public String writeNotUpdatedItemsExcel(List<ItemHbRepository.ItemUpdateError> data, List<String> headers, String folderName) throws IOException {

        try (XSSFWorkbook workbook =new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Items");
            applyDefaultColumnWidths(sheet);

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            populateHeaders(headerRow,headers,headerStyle);

            CellStyle dataStyle = createDataStyle(workbook);
            populateDataRows(sheet,data,headers,dataStyle);

            return saveWorkbookForNotUpdated(workbook,folderName);
        }
    }



    private void applyDefaultColumnWidths(Sheet sheet) {
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);

        style.setFont(font);
        return style;
    }

    private CellStyle createDataStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        return style;
    }

    private void populateHeaders(Row headerRow, List<String> headers, CellStyle style) {
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(style);
        }
    }

    private void populateDataRows(Sheet sheet,
                                  Map<String, List<Object>> data,
                                  List<String> headers,
                                  int totalRows,
                                  CellStyle style) {

        for (int rowIdx = 0; rowIdx < totalRows; rowIdx++) {
            Row row = sheet.createRow(rowIdx + 1);

            for (int colIdx = 0; colIdx < headers.size(); colIdx++) {
                String originalHeader = headers.get(colIdx);
                String key = normalizeHeader(originalHeader);

                Cell cell = row.createCell(colIdx);
                String value = formatValue(key, data.get(key) != null ? data.get(key).get(rowIdx) : "");

                cell.setCellValue(value);
                cell.setCellStyle(style);
            }
        }
    }


    private void populateDataRows(Sheet sheet,
                                  List<ItemHbRepository.ItemUpdateError> errors,
                                  List<String> headers,
                                  CellStyle style) {
        int rowNum = 1;

        for (ItemHbRepository.ItemUpdateError error : errors) {
            Map<String, Object> itemDetail = error.getItemRowDetail();
            Row row = sheet.createRow(rowNum++);

            for (int col = 0; col < headers.size(); col++) {
                String header = headers.get(col);
                Cell cell = row.createCell(col);

                String value = header.equals("REASON")
                        ? error.getErrorMessage()
                        : String.valueOf(itemDetail.getOrDefault(header, ""));

                cell.setCellValue(value);
                cell.setCellStyle(style);
            }
        }
    }

    private String normalizeHeader(String header) {
        String cleaned = header.replace("-", "");
        return "TOKEN".equals(cleaned) ? "SLUG" : cleaned;
    }

    private String formatValue(String key, Object rawValue) {
        String value = String.valueOf(rawValue);

        // Special value mappings (can be easily extended later)
        return switch (key) {
            case "LABEL"   -> mapLabel(value);
            case "STATUS"  -> mapStatus(value);
            case "INSTOCK" -> mapInStock(value);
            case "CREATEDAT", "UPDATEDAT" -> formatDate(value);
            default -> value;
        };
    }

    private String mapLabel(String value) {
        return switch (value) {
            case "N" -> "New";
            case "O" -> "Old";
            default  -> value;
        };
    }

    private String mapStatus(String value) {
        return switch (value) {
            case "A" -> "Active";
            case "D" -> "Deactive";
            default  -> value;
        };
    }

    private String mapInStock(String value) {
        return switch (value) {
            case "Y" -> "Yes";
            case "N" -> "No";
            default  -> value;
        };
    }

    private String formatDate(String value) {
        try {
            long millis = ((Double) Double.parseDouble(value)).longValue();
            return Utils.getMillisToDate(millis);
        } catch (Exception e) {
            return value; // fallback - better to safeLog in real code
        }
    }

    private String saveWorkbook(XSSFWorkbook workbook, String folderName) throws IOException {
        Path basePath = Paths.get(excelExportAbsolutePath);
        Path targetDir = basePath.resolve(folderName).normalize();

        File directory = targetDir.toFile();
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                logger.debug("Directory created: {}", directory.getAbsolutePath());
            }
        }

        String filePath = directory.getAbsolutePath() + File.separator + "temp.xlsx";

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }

        return filePath;
    }


    private String saveWorkbookForNotUpdated(XSSFWorkbook workbook, String folderName) throws IOException {
        File directory = new File(getExcelNotUpdateItemsFolderPath + folderName);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                logger.debug("Created directory for not-updated items: {}", directory.getAbsolutePath());
            }
        }

        String fileName = "temp.xlsx";
        String fullPath = directory.getAbsolutePath() + File.separator + fileName;

        try (FileOutputStream fos = new FileOutputStream(fullPath)) {
            workbook.write(fos);
        }

        logger.debug("Not updated items excel saved at: {}", fullPath);

        return fileName;
    }


}
