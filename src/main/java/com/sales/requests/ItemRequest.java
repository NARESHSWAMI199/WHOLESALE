package com.sales.requests;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sales.entities.ItemCategory;
import com.sales.entities.ItemSubCategory;
import com.sales.request.enums.ITEM_LABEL;
import com.sales.request.enums.ITEM_STOCK;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@ToString
public class ItemRequest {
    @NotNull
    @NotBlank
    private String name;
    @NotNull
    @NotBlank
    private String wholesaleSlug;
    @NotNull
    private Float price;
    @NotNull
    private Float discount;
    private Float rating = 0f;
    @NotNull
    @NotBlank
    private String description;
    private String inStock = ITEM_STOCK.OUT_OF_STOCK.getStock();  // default not in stock
    private String slug;
    private String label = ITEM_LABEL.NEW.getLabel(); // default label is new
    private Float capacity;
    private MultipartFile itemImage;
    @JsonIgnore
    private Integer storeId;
    @NotNull
    private Integer categoryId;
    @NotNull
    private Integer subCategoryId;
    @JsonIgnore
    private ItemCategory itemCategory;
    @JsonIgnore
    private ItemSubCategory itemSubCategory;
    private String previousItemImages;
    private List<MultipartFile> newItemImages;

}
