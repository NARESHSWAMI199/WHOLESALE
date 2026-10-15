package com.sales.readers;

import com.sales.admin.dto.ItemDto;
import com.sales.admin.repositories.ItemRepository;
import com.sales.admin.services.ItemService;
import com.sales.claims.AuthUser;
import com.sales.entities.Item;
import com.sales.request.ItemFilterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.Iterator;
import java.util.List;


@RequiredArgsConstructor
public class ServiceItemReader implements ItemReader<ItemDto> {

    private int pageNo = 0;
    /** this will help to get 1 item at a time.
     because when itemReader call read method call its CHUNKS_NUMBER and only one item at once
     */
    private Iterator<ItemDto> itemChunksIterator;


    private final ItemService itemService;
    private final ItemFilterRequest searchFilters;
    private final String wholesaleSlug;
    private final AuthUser loggedUser;

    @Override
    public ItemDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (itemChunksIterator == null || !itemChunksIterator.hasNext()) {
            searchFilters.setPageNumber(pageNo);
            searchFilters.setSize(100);
            List<ItemDto> items = itemService.fetchDataForExcelSheet(searchFilters, wholesaleSlug, loggedUser);
            if (items.isEmpty()) {
                return null;
            }
            this.itemChunksIterator = items.iterator();
            pageNo++;
        }
        return itemChunksIterator.next();
    }
}
