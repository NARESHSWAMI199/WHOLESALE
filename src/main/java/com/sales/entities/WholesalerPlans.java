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
    int id;
    @Column(name = "slug")
    String slug;
    @Column(name = "user_id")
    Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", referencedColumnName = "id")
    ServicePlan servicePlan;
    @Column(name = "created_at")
    Long createdAt;
    @Column(name = "expiry_date")
    Long expiryDate;
    @Column(name = "created_by")
    Integer createdBy;

    @Transient
    boolean isExpired;

}
