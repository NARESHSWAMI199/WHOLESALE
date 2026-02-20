package com.sales.chats.repositories;


import com.sales.dto.ChatRoomDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor
public class ChatRoomHbRepository {

    private final EntityManager entityManager;


    public int updateChatRoom(ChatRoomDto chatRoomDto) {
        // TODO : Must add updated_by to db side and also here.
        String hql = "update set name=:name,description =:description where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("name", chatRoomDto.getName());
        query.setParameter("description",chatRoomDto.getDescription());
        query.setParameter("slug",chatRoomDto.getSlug());
        return query.executeUpdate();
    }


}
