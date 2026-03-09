package com.sales.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sales.entities.StoreCategory;
import com.sales.entities.StoreSubCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class StoreCreationRequest extends AddressRequest {
    @NotNull
    @NotBlank
    private String storeEmail;
    private String userSlug;
    @NotNull
    @NotBlank
    private String storeName;
    private Float rating = 0f;
    private String status;
    @NotNull
    @NotBlank
    private String storePhone;
    private String storeSlug;
    @NotNull
    private String description;
    private String storeAvatar;
    @NotNull
    private MultipartFile storePic;
    private List<Long> seenIds ;
    @NotNull
    private Integer categoryId;
    @NotNull
    private Integer subCategoryId;
    @JsonIgnore
    private StoreCategory storeCategory;
    @JsonIgnore
    private StoreSubCategory storeSubCategory;
}
