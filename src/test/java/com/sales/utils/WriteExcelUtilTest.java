package com.sales.utils;

import com.sales.admin.repositories.ItemHbRepository;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WriteExcelUtilTest {

    @Test
    void testCreateExcelSheet_andSave() throws Exception {
        WriteExcelUtil writer = new WriteExcelUtil();

        // prepare temporary directory
        File tmp = Files.createTempDirectory("excel-test").toFile();
        tmp.deleteOnExit();

        // set the field via reflection
        Field f = WriteExcelUtil.class.getDeclaredField("excelExportAbsolutePath");
        f.setAccessible(true);
        f.set(writer, tmp.getAbsolutePath());

        Map<String, List<Object>> data = new HashMap<>();
        data.put("SLUG", Arrays.asList("s1", "s2"));
        data.put("LABEL", Arrays.asList("N", "O"));
        data.put("STATUS", Arrays.asList("A", "D"));
        data.put("INSTOCK", Arrays.asList("Y", "N"));
        data.put("CREATEDAT", Arrays.asList(String.valueOf(1000.0), String.valueOf(2000.0)));

        List<String> headers = Arrays.asList("SLUG", "LABEL", "STATUS", "INSTOCK", "CREATEDAT");

        String path = writer.createExcelSheet(data, 2, headers, "folder1");
        File fpath = new File(path);
        assertTrue(fpath.exists());
        // cleanup
        fpath.delete();
        new File(tmp, "folder1").delete();
    }

    @Test
    void testWriteNotUpdatedItemsExcel() throws Exception {
        WriteExcelUtil writer = new WriteExcelUtil();

        File tmp = Files.createTempDirectory("excel-test-2").toFile();
        tmp.deleteOnExit();

        Field f = WriteExcelUtil.class.getDeclaredField("getExcelNotUpdateItemsFolderPath");
        f.setAccessible(true);
        f.set(writer, tmp.getAbsolutePath() + File.separator);

        ItemHbRepository.ItemUpdateError err = new ItemHbRepository.ItemUpdateError();
        Map<String, Object> detail = new HashMap<>();
        detail.put("ID", "1");
        err.setItemRowDetail(detail);
        err.setErrorMessage("bad");

        List<String> headers = Arrays.asList("ID", "REASON");

        String fileName = writer.writeNotUpdatedItemsExcel(Arrays.asList(err), headers, "folder2");
        File saved = new File(tmp, "folder2" + File.separator + fileName);
        assertTrue(saved.exists());

        // cleanup
        saved.delete();
        new File(tmp, "folder2").delete();
    }
}
