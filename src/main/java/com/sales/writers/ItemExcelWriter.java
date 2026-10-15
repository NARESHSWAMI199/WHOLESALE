package com.sales.writers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sales.admin.dto.ItemDto;
import com.sales.admin.repositories.ItemHbRepository;
import com.sales.claims.AuthUser;
import com.sales.global.GlobalConstant;
import com.sales.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ItemExcelWriter implements ItemStreamWriter<ItemDto> {

    private final Logger logger = LoggerFactory.getLogger(ItemExcelWriter.class);
    private final List<String> headers = GlobalConstant.HEADERS_FOR_ITEMS;
    private final String wholesaleSlug;
    private final AuthUser loggedUser;



    private SXSSFWorkbook workbook;
    private Sheet sheet;
    private int rowNo = 0;

    @Value("${excel.export.absolute}")
    String excelExportAbsolutePath;

    @Value("${excel.notUpdated.absolute}")
    String getExcelNotUpdateItemsFolderPath;





    @Override
    public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        logger.info("Opening Excel File");
        workbook = new SXSSFWorkbook(100);
        sheet = workbook.createSheet("Items");
        applyDefaultColumnWidths(sheet);

        Row headerRow = sheet.createRow(rowNo++);
        CellStyle headerStyle = createHeaderStyle(workbook);
        populateHeaders(headerRow, headers, headerStyle);
        logger.info("Created Excel File");
    }

    @Override
    public void write(Chunk<? extends ItemDto> chunk) throws Exception {
        Map<String, List<Object>> result = new HashMap<>();
        for (ItemDto item : chunk.getItems()) {
            Gson itemsGson = new GsonBuilder().serializeNulls().create();
            String items = itemsGson.toJson(item);
            Map<String, Object> itemMap = new Gson().fromJson(items, Map.class);
            itemMap.forEach((key, value) -> {
                if (key.equals("wholesale")) {
                    // skip...
                } else if (result.containsKey(key.toUpperCase())) {
                    result.get(key.toUpperCase()).add(itemMap.get(key));
                } else {
                    List<Object> valueList = new ArrayList<>();
                    valueList.add(value);
                    result.put(key.toUpperCase(), valueList);
                }
            });
        }
        CellStyle dataStyle = createDataStyle(workbook);
        populateDataRows(sheet, result,headers,chunk.getItems().size(),dataStyle);
    }

    @Override
    public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
       // doing nothing here...
    }

    @Override
    public void close() {
        logger.info("Going to close workbook.");
        if (workbook != null) {
            String folderName = !Utils.isEmpty(wholesaleSlug) ? wholesaleSlug : loggedUser.getSlug();
            // When we're creating all items, excel without a specific user wholesale or store from admin pannel
            if (folderName == null) folderName = loggedUser.getSlug();

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
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try { workbook.close(); } catch (IOException ignored) {}
            }
            logger.info("Workbook closed.");
        }
    }


    public String writeNotUpdatedItemsExcel(List<ItemHbRepository.ItemUpdateError> data, List<String> headers, String folderName) throws IOException {

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("Items");
            applyDefaultColumnWidths(sheet);

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            populateHeaders(headerRow, headers, headerStyle);

            CellStyle dataStyle = createDataStyle(workbook);
            populateDataRows(sheet, data, headers, dataStyle);

            return saveWorkbookForNotUpdated(workbook, folderName);
        }
    }


    private void applyDefaultColumnWidths(Sheet sheet) {
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);
    }

    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);

        style.setFont(font);
        return style;
    }

    private CellStyle createDataStyle(SXSSFWorkbook workbook) {
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
            Row row = sheet.createRow(rowNo++);
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
            case "LABEL" -> mapLabel(value);
            case "STATUS" -> mapStatus(value);
            case "INSTOCK" -> mapInStock(value);
            case "CREATEDAT", "UPDATEDAT" -> formatDate(value);
            default -> value;
        };
    }

    private String mapLabel(String value) {
        return switch (value) {
            case "N" -> "New";
            case "O" -> "Old";
            default -> value;
        };
    }

    private String mapStatus(String value) {
        return switch (value) {
            case "A" -> "Active";
            case "D" -> "Deactive";
            default -> value;
        };
    }

    private String mapInStock(String value) {
        return switch (value) {
            case "Y" -> "Yes";
            case "N" -> "No";
            default -> value;
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

    private String saveWorkbookForNotUpdated(SXSSFWorkbook workbook, String folderName) throws IOException {
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
