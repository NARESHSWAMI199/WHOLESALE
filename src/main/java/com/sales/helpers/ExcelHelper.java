package com.sales.helpers;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class ExcelHelper {

    public static final String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String TYPE_XLS = "application/vnd.ms-excel";
    private static final List<String> EXCEL_MIME_TYPES = Arrays.asList(TYPE, TYPE_XLS);
    private ExcelHelper() {
    }

    public static boolean hasExcelFormat(MultipartFile file) {
        if (!EXCEL_MIME_TYPES.contains(file.getContentType())) {
            return false;
        }
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            workbook.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
