package com.sales.requests;


import com.sales.entities.ItemCategory;
import com.sales.entities.ItemSubCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@ToString
public class ItemRequest {
    private String name;
    private String wholesaleSlug;
    private Float price;
    private Float discount;
    private Float rating = 0f;
    private String description;
    private String InStock;
    private  String slug;
    private String label;
    private  Float capacity;
    private MultipartFile itemImage;
    private Integer storeId;
    private Integer categoryId = 0;
    private Integer subCategoryId = 0;
    private ItemCategory itemCategory;
    private ItemSubCategory itemSubCategory;
    private String previousItemImages;
    private List<MultipartFile> newItemImages;

}
