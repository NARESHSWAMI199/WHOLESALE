package com.sales.request;


import com.sales.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchFilters {
    protected String searchKey ="";
//    protected String name = "";
//    protected String status;
    protected Long fromDate=0L;
    protected Long toDate= Utils.getCurrentMillis();
    protected String orderBy="id";
    protected String order = "desc";
    protected int pageNumber = 0;
    protected int size = 10;
//    protected String slug="";
//    protected int storeId;
//    protected long itemId;
//    protected Integer categoryId;
//    protected Integer subCategoryId;
}
