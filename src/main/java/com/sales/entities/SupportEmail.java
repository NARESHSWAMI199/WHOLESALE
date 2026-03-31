package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "support_emails")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SupportEmail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "email")
    String email;

    @Column(name = "password_key")
    String passwordKey;

    @Column(name = "support_type")
    String supportType;

}
