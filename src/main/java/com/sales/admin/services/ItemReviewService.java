package com.sales.admin.services;

import com.sales.admin.dto.ItemReviewDto;
import com.sales.admin.mapper.ItemReviewMapper;
import com.sales.admin.repositories.ItemReviewRepository;
import com.sales.dto.ItemReviewsFilterDto;
import com.sales.entities.ItemReviews;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sales.helpers.PaginationHelper.getPageable;
import static com.sales.specifications.ItemReviewSpecifications.*;


@Service
@RequiredArgsConstructor
public class ItemReviewService {

    private final ItemReviewRepository itemReviewRepository;
    private final ItemReviewMapper itemReviewMapper;
  
  private static final Logger logger = LoggerFactory.getLogger(ItemReviewService.class);

    @Transactional(readOnly = true)
    public Page<ItemReviewDto> getAllItemReview(ItemReviewsFilterDto filters) {
        logger.debug("Entering getALlItemReview with filters: {}", filters);
        Specification<ItemReviews> specification = Specification.allOf(
                (containsName(filters.getSearchKey()))
                        .and(greaterThanOrEqualFromDate(filters.getFromDate()))
                        .and(lessThanOrEqualToToDate(filters.getToDate()))
                        .and(hasSlug(filters.getSlug()))
                        .and(isItemId(filters.getItemId()))
                        .and(isParentComment(filters.getParentId()))
        );
        Pageable pageable = getPageable(logger,filters);
        Page<ItemReviews> itemReviewsPage = itemReviewRepository.findAll(specification,pageable);
        return itemReviewsPage.map(itemReviewMapper::toDto);
    }


}
