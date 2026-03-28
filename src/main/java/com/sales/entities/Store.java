package com.sales.entities;

import com.sales.claims.AuthUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.util.UUID;

import static com.sales.utils.Utils.getCurrentMillis;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "stores")
@SQLRestriction("is_deleted != 'Y'") /* Same as where clause */
@Builder
public class Store implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    int id;
    @Column(name = "slug")
    String slug;
    @Column(name = "name")
    String storeName;
    @Column(name = "avatar")
    String avtar;
    @Column(name = "email")
    String email;
    @Column(name = "phone")
    String phone;
    @Column(name = "description")
    String description;
    @Column(name = "rating")
    Float rating;
    @Column(name = "status")
    String status;
    @Column(name = "is_deleted")
    String isDeleted="N";
    @Column(name = "created_at")
    Long createdAt;
    @Column(name = "created_by")
    Integer createdBy;
    @Column(name = "updated_at")
    Long updatedAt;
    @Column(name = "updated_by")
    Integer updatedBy;
    @OneToOne
    @JoinColumn(name = "user_id")
    User user;

    @OneToOne
    @JoinColumn(name = "address_id")
    Address address;

    @Transient
    Integer totalStoreItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    StoreCategory storeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", referencedColumnName = "id")
    StoreSubCategory storeSubCategory;



    public Store (AuthUser loggedUser) {
        this.createdAt = getCurrentMillis();
        this.createdBy = loggedUser.getId();
        this.updatedAt = getCurrentMillis();
        this.updatedBy = loggedUser.getId();
        this.status = "A";
        this.isDeleted = "N";
        this.slug = UUID.randomUUID().toString();
    }

}
