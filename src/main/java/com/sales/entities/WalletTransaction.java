package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Table(name = "wallet_transactions")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WalletTransaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "slug")
    String slug;

    @Column(name = "user_id")
    Integer userId;

    @Column(name = "amount")
    Float amount;

    @Column(name = "created_at")
    Long createdAt;

    @Column(name = "transaction_type")
    String transactionType;

    @Column(name = "status")
    String status;

}
