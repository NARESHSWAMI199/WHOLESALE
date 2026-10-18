package com.sales.springbatch.readers;

import com.sales.admin.dto.ItemDto;
import com.sales.admin.mapper.ItemMapper;
import com.sales.admin.services.ItemService;
import com.sales.claims.AuthUser;
import com.sales.entities.Item;
import com.sales.request.ItemFilterRequest;
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
public class ProductItemReader implements ItemReader<ItemDto> {

    private int pageNo = 0;
    /** this will help to get 1 item at a time.
     because when itemReader call read method call its CHUNKS_NUMBER and only one item at once
     */
    private Iterator<ItemDto> itemChunksIterator;

    private final ItemService itemService;
    private final ItemFilterRequest searchFilters;
    private final String wholesaleSlug;
    private final AuthUser loggedUser;
    private final StepExecution stepExecution;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (itemChunksIterator == null || !itemChunksIterator.hasNext()) {
            searchFilters.setPageNumber(pageNo);
            searchFilters.setSize(100);
            Page<Item> itemPage = itemService.fetchDataForExcelSheet(searchFilters, wholesaleSlug, loggedUser);
            List<ItemDto> items = itemPage.getContent().stream().map(itemMapper::toDto).toList();
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
