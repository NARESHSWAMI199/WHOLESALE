package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Table(name = "user_wallet")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Wallet implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "amount")
    private Float amount;

    @Column(name = "updated_at")
    private Long updatedAt;

}
