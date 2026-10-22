package com.sales.springbatch.readers;

import com.sales.exceptions.MyException;
import com.sales.global.ResponseMessages;
import com.sales.requests.ItemRequest;
import com.sales.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ExcelStreamReader implements ItemReader<ItemRequest> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String filePath;
    private InputStream inputStream;
    private ReadableWorkbook workbook;
    private Stream<Row> rowStream;
    private Iterator<Row> rowIterator;
    private boolean isInitialized = false;
    private List<String> headers;

    private void initialize() throws Exception {
        this.headers = new ArrayList<>();
        this.inputStream = new FileInputStream(filePath);
        this.workbook = new ReadableWorkbook(inputStream);
        Sheet sheet = workbook.getFirstSheet();
        this.rowStream = sheet.openStream();
        this.rowIterator = rowStream.iterator();
        if (rowIterator.hasNext()) {
            Row headerRow = rowIterator.next();
            for (Cell cell : headerRow) {
                if (cell != null) {
                    headers.add(cell.getText().toUpperCase());
                }
            }
        }
        this.isInitialized = true;
    }


    @Override
    public ItemRequest read() throws Exception {
        if (!isInitialized) {
            initialize();
        }

        if (rowIterator == null || !rowIterator.hasNext()) {
            closeResources();
            return null;
        }

        Row row = rowIterator.next();
        List<String> prefix = List.of("N", "O", "Y");
        ItemRequest.ItemRequestBuilder builder = ItemRequest.builder();
        int index = 0;
        logger.info("header header: {}", headers);
        for (String header : headers) {
            String cellText = row.getCellText(index);
            logger.info("cell text: {}", cellText);
            switch (header) {
                case "NAME" -> builder.name(cellText);
                case "LABEL" -> {
                    builder.label((!Utils.isEmpty(cellText) && prefix.contains(cellText.charAt(0) + "")) ? String.valueOf(cellText.charAt(0)).toUpperCase() : "N");
                }
                case "IN-STOCK" ->
                        builder.inStock(!Utils.isEmpty(cellText) && prefix.contains(cellText.charAt(0) + "") ? String.valueOf(cellText.charAt(0)).toUpperCase() : "N");
                case "TOKEN" -> builder.slug(cellText);
                case "CAPACITY" -> builder.capacity(Utils.isEmpty(cellText) ? 0f : Float.parseFloat(cellText));
                case "PRICE" -> builder.price(Utils.isEmpty(cellText) ? 0f : Float.parseFloat(cellText));
                case "DISCOUNT" -> builder.discount(Utils.isEmpty(cellText) ? 0f : Float.parseFloat(cellText));
                default -> {
                }
            }
            index++;
        }
        ItemRequest build = builder.build();
        logger.info("build: {}", build);
        if (build.getPrice() < build.getDiscount()) {
            throw new MyException(ResponseMessages.PRICE_CAN_T_BE_LESS_THEN_DISCOUNT);
        }
        return build;
    }


    private void closeResources() {
        try {
            if (rowStream != null) rowStream.close();
            if (workbook != null) workbook.close();
            if (inputStream != null) inputStream.close();
        } catch (Exception ignored) {
        }
    }
}

