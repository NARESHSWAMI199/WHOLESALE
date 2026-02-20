package com.sales.chats.repositories;


import com.sales.claims.AuthUser;
import com.sales.entities.ChatUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser,Integer> {

    @Query("""
    from ChatUser cu 
        left join User u on cu.chatUser.id = u.id 
        left join Chat c on u.slug = c.receiver 
    where cu.userId=:userId order by c.createdAt desc""")
   List<ChatUser> getChatUserByUserId(Integer userId);

    @Query("select senderAcceptStatus from ChatUser where userId=:userId and chatUser=:chatUser")
    String getSenderAcceptStatus(Integer userId,AuthUser chatUser);


    ChatUser findByUserIdAndChatUser(Integer userId , AuthUser chatUser);

    @Modifying
    @Transactional
    @Query("delete from ChatUser where userId=:userId and chatUser=:chatUser")
    Integer deleteChatUserFromChatList(Integer userId,AuthUser chatUser);
}
