package com.sales.chats.repositories;


import com.sales.claims.AuthUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class ChatUserHbRepository {

    private final EntityManager entityManager;


    public boolean updateAcceptStatus(Integer userId, AuthUser chatUser, String senderAcceptStatus) {
        String hql = "update ChatUser set senderAcceptStatus=:senderAcceptStatus where userId=:userId and chatUser =:chatUser ";
        Query query = entityManager.createQuery(hql);
        query.setParameter("senderAcceptStatus", senderAcceptStatus);
        query.setParameter("userId", userId);
        query.setParameter("chatUser", chatUser);
        return query.executeUpdate() > 0;
    }

}
