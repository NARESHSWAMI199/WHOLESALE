package com.sales.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemReviewsFilterDto extends SearchFilters{
    String message;
    Integer userId;
    String userSlug;
    String itemSlug;
    long itemId;
    int parentId = 0;
}
