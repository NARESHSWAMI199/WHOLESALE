package com.sales.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "item_reviews")
@SQLRestriction("is_deleted != 'Y'")
public class ItemReviews implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "item_id")
    Long itemId;

    @Column(name = "rating")
    Float rating;

    @Column(name = "slug")
    String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "likes")
    Long likes = 0L;

    @Column(name = "parent_id")
    Integer parentId;

    @Column(name = "message")
    String message;

    @Column(name = "created_at")
    String createdAt;

    @Column(name = "is_deleted")
    String isDeleted;

    @Column(name = "updated_at")
    String updatedAt;

    @Transient
    Integer repliesCount;
//
//    @Transient
//    String username;
//    @Transient
//    String avatar;
//    @Transient
//    String userSlug;

}
