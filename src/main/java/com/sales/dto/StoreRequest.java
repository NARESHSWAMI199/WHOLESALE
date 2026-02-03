package com.sales.dto;

import com.sales.entities.StoreCategory;
import com.sales.entities.StoreSubCategory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class StoreRequest extends AddressRequest {
    private String storeEmail;
    private String userSlug;
    private String storeName;
    private Float rating = (float) 0;
    private String status;
    private String storePhone;
    private String storeSlug;
    private String description="";
    private String storeAvatar;
    private MultipartFile storePic;
    private List<Long> seenIds ;
    private Integer categoryId;
    private Integer subCategoryId;
    private StoreCategory storeCategory;
    private StoreSubCategory storeSubCategory;
}
