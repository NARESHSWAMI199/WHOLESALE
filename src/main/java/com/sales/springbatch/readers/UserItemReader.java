package com.sales.springbatch.readers;

import com.sales.admin.dto.ItemDto;
import com.sales.admin.dto.UserDto;
import com.sales.admin.mapper.UserMapper;
import com.sales.admin.services.UserService;
import com.sales.claims.AuthUser;
import com.sales.entities.User;
import com.sales.request.ItemFilterRequest;
import com.sales.request.UserSearchFilters;
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
public class UserItemReader implements ItemReader<UserDto> {

    private int pageNo = 0;
    /** this will help to get 1 item at a time.
     because when itemReader call read method call its CHUNKS_NUMBER and only one item at once
     */
    private Iterator<UserDto> itemChunksIterator;

    private final UserService userService;
    private final UserSearchFilters searchFilters;
    private final AuthUser loggedUser;
    private final StepExecution stepExecution;
    private final UserMapper userMapper;

    @Override
    public UserDto read() {
        if (itemChunksIterator == null || !itemChunksIterator.hasNext()) {
            searchFilters.setPageNumber(pageNo);
            searchFilters.setSize(100);
            Page<User> itemPage = userService.fetchDataForExcelSheet(searchFilters, loggedUser);
            List<UserDto> items = itemPage.getContent().stream().map(userMapper::toDto).toList();
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
