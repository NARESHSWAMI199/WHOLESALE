package com.sales.helpers;

import com.sales.request.SearchFilters;
import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationHelper {
    private PaginationHelper(){}
    public static Pageable getPageable(Logger logger, SearchFilters filters){
        logger.debug("page : {} {}", filters.getPageNumber(), filters.getSize());
        Sort sort = (filters.getOrder().equalsIgnoreCase("asc")) ?
                Sort.by(filters.getOrderBy()).ascending() :  Sort.by(filters.getOrderBy()).descending();
        return PageRequest.of(filters.getPageNumber(), filters.getSize(),sort);
    }
}
