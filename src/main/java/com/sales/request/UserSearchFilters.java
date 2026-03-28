package com.sales.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserSearchFilters extends SearchFilters {
    private String slug;
    private String status;
    private String userType;
}
