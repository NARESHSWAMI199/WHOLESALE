package com.sales.utils;

import com.sales.exceptions.MyException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class ReadExcel {


    private final Logger logger = LoggerFactory.getLogger(ReadExcel.class);

    public Map<String, List<String>> getExcelDataInJsonFormat(MultipartFile excelFile) {

        Map<String, List<String>> result = new HashMap<>();
        List<String> columnsList = new ArrayList<>();
        try {
            DataFormatter formatter = new DataFormatter();
            logger.debug("Excel file reading....");
            XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());

            int totalSheets = workbook.getNumberOfSheets();

            /** for visit worksheet */
            for (int sheetNumber = 0; sheetNumber < totalSheets; sheetNumber++) {
                XSSFSheet worksheet = workbook.getSheetAt(sheetNumber);
                int totalColumns = 0; // Initialize totalColumns for each sheet

                if (worksheet.getPhysicalNumberOfRows() > 0) {
                    XSSFRow firstRow = worksheet.getRow(0);
                    if (firstRow != null) {
                        totalColumns = firstRow.getPhysicalNumberOfCells(); // Dynamically get column count
                    }
                }
                /** for visit rows */
                for (int rowNumber = 0; rowNumber < worksheet.getPhysicalNumberOfRows(); rowNumber++) {
                    XSSFRow row = worksheet.getRow(rowNumber);
                    /** for visit columns */
                    for (int colNumber = 0; colNumber < totalColumns; colNumber++) {
                        XSSFCell column = row.getCell(colNumber);
                        if (rowNumber == 0) {
                            /** column the first row */
                            columnsList.add(column.getStringCellValue());
                            result.put(column.getStringCellValue().toUpperCase(), new ArrayList<>());
                        } else {
                            String columnName = columnsList.get(colNumber);
                            List<String> updatedList = result.get(columnName);
                            String val = formatter.formatCellValue(column);
                            updatedList.add(val);
                            result.put(columnName, updatedList);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception during creating excel file : {} ", e.getMessage());
            throw new MyException(e.getMessage());
        }
        logger.debug("Excel file reading END.... ");
        return result;
    }

}
