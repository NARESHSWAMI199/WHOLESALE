package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;

@Entity
@Table(name = "service_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted != 'Y'")
public class ServicePlan implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Column(name = "slug")
    String slug;
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "price", nullable = false)
    Long price;
    @Column(name = "discount", nullable = false)
    Long discount;
    @Column(name = "status")
    String status;
    @Column(name = "icon")
    String icon;
    @Column(name = "months", nullable = false)
    Integer months;
    @Column(name = "description")
    String description;
    @Column(name = "is_deleted")
    String isDeleted;
    @Column(name = "created_at", nullable = false)
    Long createdAt;
    @Column(name = "created_by", nullable = false)
    Integer createdBy;
    @Column(name = "updated_at", nullable = false)
    Long updatedAt;
    @Column(name = "updated_by", nullable = false)
    Integer updatedBy;
}
