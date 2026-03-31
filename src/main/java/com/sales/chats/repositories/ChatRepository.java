package com.sales.chats.repositories;

import com.sales.entities.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query(" from Chat where (sender=:sender and receiver=:receiver) or (sender=:receiver and receiver=:sender)")
    List<Chat> getChatBySenderKeyOrReceiverKey(String sender, String receiver);


    @Query(" select count(id) as count from Chat where (sender=:sender and receiver=:receiver) and seen=false and isSent='S' ")
    Integer getUnSeenChatsCount(String sender, String receiver);


    @Query("select count(id) as count from Chat where sender=:sender and receiver=:receiver")
    Integer isUserExistsInChatList(String sender, String receiver);


    @Query("from Chat where sender=:sender and receiver=:receiver and createdAt=:createdAt")
    Optional<Chat> getParentMessageByCreateAt(String sender, String receiver, Long createdAt);

    @Query("select id from Chat where sender=:sender and receiver=:receiver and createdAt=:createdAt")
    Integer getParentMessageIdByCreateAt(String sender, String receiver, Long createdAt);


}
