package com.sales.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "item_ratings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemRating implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "item_id")
    Long itemId;

    @Column(name = "user_id")
    Integer userId;

    @Column(name = "rating")
    Integer rating;

    @Column(name = "created_at")
    Long createdAt;

    @Column(name = "updated_at")
    Long updatedAt;


}
