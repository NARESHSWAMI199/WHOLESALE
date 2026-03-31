package com.sales.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "store_promotions")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class StorePromotion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "banner_img")
    String bannerImage;

    @Column(name = "promotion_type")
    String promotionType;

    @Column(name = "store_id")
    Integer storeId;

    @Column(name = "item_id")
    Long itemId;

    @Column(name = "priority")
    String priority;

    @Column(name = "priority_hours")
    Long priorityHours;

    @Column(name = "max_repeat")
    Integer maxRepeat;

    @Column(name = "state")
    Integer stateId;

    @Column(name = "city")
    Integer cityId;

    @Column(name = "created_date")
    Integer createdDate;

    @Column(name = "start_date")
    Long startDate;

    @Column(name = "expiry_date")
    Long expiryDate;

    @Column(name = "created_by")
    Integer createdBy;

    @Column(name = "is_deleted")
    String isDeleted;


}
