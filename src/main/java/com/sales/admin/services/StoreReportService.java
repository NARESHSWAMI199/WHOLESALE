package com.sales.admin.services;


import com.sales.admin.repositories.StoreReportRepository;
import com.sales.request.SearchFilters;
import com.sales.entities.StoreReport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import static com.sales.helpers.PaginationHelper.getPageable;
import static com.sales.specifications.StoreReportSpecifications.hasStoreId;

@Service
@RequiredArgsConstructor
public class StoreReportService {

    private static final Logger logger = LoggerFactory.getLogger(StoreReportService.class);
    private final StoreReportRepository storeReportRepository;

    public Page<StoreReport> getAllReportByStoreId(SearchFilters searchFilters){
        Pageable pageable = getPageable(logger,searchFilters);
        Specification<StoreReport> specification = Specification.allOf(hasStoreId(searchFilters.getStoreId()));
        return storeReportRepository.findAll(specification,pageable);
    }


}
