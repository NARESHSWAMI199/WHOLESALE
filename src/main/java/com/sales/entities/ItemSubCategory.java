package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;

@Entity(name = "ItemSubCategory")
@Table(name = "item_subcategories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted !='Y'")
public class ItemSubCategory implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "slug")
    String slug;

    @Column(name = "category_id")
    Integer categoryId;

    @Column(name = "subcategory")
    String subcategory;

    @Column(name = "unit")
    String unit;

    @Column(name = "icon")
    String icon;

    @Column(name = "updated_at")
    Long updatedAt;

    @Column(name = "is_deleted")
    String isDeleted = "N";


}
