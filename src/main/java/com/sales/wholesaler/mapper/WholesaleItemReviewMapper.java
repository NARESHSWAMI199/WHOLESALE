package com.sales.wholesaler.mapper;


import com.sales.entities.ItemReviews;
import com.sales.wholesaler.dto.WholesaleItemReviewDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesaleItemReviewMapper {
    WholesaleItemReviewDto toDto(ItemReviews itemReviews);
}
