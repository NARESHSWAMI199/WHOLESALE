package com.sales.admin.mapper;


import com.sales.admin.dto.ItemReviewDto;
import com.sales.entities.ItemReviews;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemReviewMapper {
    ItemReviewDto toDto(ItemReviews itemReviews);
}
