package com.sales.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;

@Entity
@Table(name = "store_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted != 'Y' ")
@Builder
public class StoreNotifications implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "user_id")
    Integer userId;

    @Column(name = "store_id")
    Integer wholesaleId;

    @Column(name = "title")
    String title;

    @Column(name = "message_body", columnDefinition = "TEXT")
    String messageBody;

    @Column(name = "created_at")
    Long createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "user_id")
    User createdBy;

    @Column(name = "is_deleted")
    String isDeleted = "N";

    @Column(name = "seen")
    String seen = "N";


//    public StoreNotifications(SalesUser){
//        this.createdBy = loggedUser;
//        this.createAt = Utils.getCurrentMillis();
//    }


}
