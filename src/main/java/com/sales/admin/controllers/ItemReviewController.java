package com.sales.admin.controllers;

import com.sales.admin.services.ItemReviewService;
import com.sales.dto.ItemReviewsFilterDto;
import com.sales.entities.ItemReviews;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/item/review")
@RequiredArgsConstructor
@Tag(name = "Item Reviews", description = "APIs for managing item reviews")
public class ItemReviewController  {

    private final ItemReviewService itemReviewService;
    private static final Logger logger = LoggerFactory.getLogger(ItemReviewController.class);

    @PostMapping("all")
    @PreAuthorize("hasAuthority('item.review.all')")
    @Operation(summary = "Get all item reviews", description = "Retrieves a paginated list of all item reviews with optional filters")
    public ResponseEntity<Page<ItemReviews>> getAllReviews(@RequestBody ItemReviewsFilterDto ItemReviewsFilterDto, HttpServletRequest httpServletRequest) {
        logger.debug("Fetching all item comments with filters: {}", ItemReviewsFilterDto);
        Page<ItemReviews > itemReviewsPage = itemReviewService.getAllItemReview(ItemReviewsFilterDto);
        return new ResponseEntity<>(itemReviewsPage, HttpStatus.OK);
    }

}
