package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Table(name = "wholesaler_future_plans")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WholesalerFuturePlan implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    long id;

    @Column(name = "user_id",nullable = false)
    Integer userId;

    @Column(name = "slug" , nullable = false)
    String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id",nullable = false)
    ServicePlan servicePlan;

    @Column(name = "created_at")
    Long createdAt;

    @Column(name = "updated_at")
    Long updatedAt;

    @Column(name="status") // status like used or not.
    String status;

}
