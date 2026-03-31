package com.sales.utils;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadExcelTest {

    @Test
    void testGetExcelDataInJsonFormat_simple() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("sheet1");
            XSSFRow header = sheet.createRow(0);
            // Use uppercase columns because implementation stores uppercase keys
            header.createCell(0).setCellValue("NAME");
            header.createCell(1).setCellValue("VALUE");

            XSSFRow row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("alice");
            row1.createCell(1).setCellValue("10");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", baos.toByteArray());

            ReadExcel reader = new ReadExcel();
            Map<String, List<String>> parsed = reader.getExcelDataInJsonFormat(file);

            assertTrue(parsed.containsKey("NAME"));
            assertEquals(1, parsed.get("NAME").size());
            assertEquals("alice", parsed.get("NAME").get(0));

            assertTrue(parsed.containsKey("VALUE"));
            assertEquals("10", parsed.get("VALUE").get(0));
        }
    }
}
