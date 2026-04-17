package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "chat_users_list")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatUser implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "user_id")
    Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_user_id")
    User chatUser;

    @Column(name = "sender_accept_status")
    String senderAcceptStatus;


}
