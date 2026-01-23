package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "contact_list")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Contact implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "user_id")
    Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    User contactUser;


}
