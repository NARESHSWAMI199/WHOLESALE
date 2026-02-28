package com.sales.request;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaginationRequest {

    int page = 0;
    int size = 10;
    String sortBy = "id";
    String order = "ASC";

}
