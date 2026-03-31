package com.sales.wholesaler.services;

import com.sales.claims.AuthUser;
import com.sales.entities.ItemReviews;
import com.sales.global.ResponseMessages;
import com.sales.request.ItemReviewsFilterRequest;
import com.sales.wholesaler.dto.WholesaleItemReviewDto;
import com.sales.wholesaler.mapper.WholesaleItemReviewMapper;
import com.sales.wholesaler.repository.WholesaleItemReviewRepository;
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
public class WholesaleItemReviewService  {

    private final WholesaleItemReviewRepository wholesaleItemReviewRepository;
    private final WholesaleItemReviewMapper wholesaleItemReviewMapper;
    private static final Logger logger = LoggerFactory.getLogger(WholesaleItemReviewService.class);

    @Transactional
    public Page<WholesaleItemReviewDto> getAllItemReview(ItemReviewsFilterRequest filters, AuthUser loggedUser) {
        logger.debug("Starting getALlItemReview method with filters: {}, loggedUser: {}", filters, loggedUser);
        if(filters.getItemId() == 0) {
            logger.error("Invalid itemId provided");
            throw new IllegalArgumentException(ResponseMessages.PLEASE_PROVIDE_A_VALID_ITEMID);
        }
        Specification<ItemReviews> specification = Specification.allOf(
                (containsName(filters.getSearchKey()))
                        .and(greaterThanOrEqualFromDate(filters.getFromDate()))
                        .and(lessThanOrEqualToToDate(filters.getToDate()))
                        .and(hasSlug(filters.getSlug()))
                        .and(isItemId(filters.getItemId()))
                        .and(isParentComment(filters.getParentId()))
        );
        Pageable pageable = getPageable(logger,filters);
            Page<ItemReviews> all = wholesaleItemReviewRepository.findAll(specification, pageable);
            return all.map(wholesaleItemReviewMapper::toDto);
    }


}
