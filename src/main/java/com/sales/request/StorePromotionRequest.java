package com.sales.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class StorePromotionRequest {
    Integer id;
    String bannerImage = "test.png";
    String promotionType;
    Integer storeId;
    Long itemId;
    String priority;
    Long priorityHours;
    Integer maxRepeat;
    Integer stateId;
    Integer cityId;
    Long startDate;
    Long expiryDate;
    MultipartFile promotionFile;
}
