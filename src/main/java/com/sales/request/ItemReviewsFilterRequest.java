package com.sales.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ItemReviewsFilterRequest extends SearchFilters {
    private String slug;
    private String message;
    private Integer userId;
    private String userSlug;
    private String itemSlug;
    private long itemId;
    private int parentId = 0;
}
