package com.sales.dto;


import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ItemReviewRequest {

    Long id;
    String username;
    String avatar;
    String slug;
    String userSlug;
    Float rating;
    String message;
    String cratedAt;
    String updatedAt;

}
