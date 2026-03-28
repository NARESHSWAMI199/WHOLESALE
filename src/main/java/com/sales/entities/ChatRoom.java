package com.sales.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;


@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoom implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    long id;

    @Column(name = "name")
    String name;

    @Column(name = "description")
    String description;

    @Column(name = "slug")
    String slug; // working as a roomId

    @Column(name = "created_at")
    Long createdAt;

    @Column(name = "updated_at")
    Long updatedAt;

    // One-to-many relationship with RoomUser
    @OneToMany(mappedBy = "roomId", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    List<ChatRoomUser> chatRoomUsers;

}
