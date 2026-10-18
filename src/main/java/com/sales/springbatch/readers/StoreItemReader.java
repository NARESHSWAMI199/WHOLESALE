package com.sales.springbatch.readers;

import com.sales.admin.dto.ItemDto;
import com.sales.admin.dto.StoreDto;
import com.sales.admin.mapper.StoreMapper;
import com.sales.admin.services.ItemService;
import com.sales.admin.services.StoreService;
import com.sales.claims.AuthUser;
import com.sales.entities.Store;
import com.sales.request.ItemFilterRequest;
import com.sales.request.StoreFilterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.data.domain.Page;

import java.util.Iterator;
import java.util.List;


@RequiredArgsConstructor
public class StoreItemReader implements ItemReader<StoreDto> {

    private int pageNo = 0;
    /** this will help to get 1 item at a time.
     because when itemReader call read method call its CHUNKS_NUMBER and only one item at once
     */
    private Iterator<StoreDto> itemChunksIterator;

    private final StoreService storeService;
    private final StoreFilterRequest searchFilters;
    private final StepExecution stepExecution;
    private final StoreMapper storeMapper;

    @Override
    public StoreDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (itemChunksIterator == null || !itemChunksIterator.hasNext()) {
            searchFilters.setPageNumber(pageNo);
            searchFilters.setSize(100);
            Page<Store> itemPage = storeService.fetchDataForExcelSheet(searchFilters);
            List<StoreDto> items = itemPage.getContent().stream().map(storeMapper::toDto).toList();
            if(pageNo == 0){
                long totalElements = itemPage.getTotalElements();
                stepExecution.getJobExecution().getExecutionContext().putLong("totalItems", totalElements);
            }
            if (items.isEmpty()) {
                return null;
            }
            this.itemChunksIterator = items.iterator();
            pageNo++;
        }
        return itemChunksIterator.next();
    }
}
