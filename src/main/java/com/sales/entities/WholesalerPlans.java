package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "wholesaler_plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WholesalerPlans implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "slug")
    private String slug;
    @Column(name = "user_id")
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", referencedColumnName = "id")
    private ServicePlan servicePlan;
    @Column(name = "created_at")
    private Long createdAt;
    @Column(name = "expiry_date")
    private Long expiryDate;
    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "plan_id",insertable = false,updatable = false)
    private Integer planId;

    @Transient
    boolean isExpired;

}
