package com.sales.chats.repositories;


import com.sales.dto.MessageDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor
public class ChatHbRepository {

    private final EntityManager entityManager;


    public boolean updateMessageToSent(long id){
        String hql = "update Chat set isSent='S' where id =:id ";
        Query query = entityManager.createQuery(hql);
        query.setParameter("id",id);
        return query.executeUpdate() > 0;
    }


    public void deleteChats(String sender , String receiver){
        String hql = """
                update Chat set
                isSenderDeleted = case when sender = :sender and receiver = :receiver 
                    then 'Y' 
                    else isSenderDeleted 
                end, 
                    isReceiverDeleted = case when receiver = :sender and sender = :receiver 
                    then 'Y' else isReceiverDeleted 
                end 
                """;
        Query query = entityManager.createQuery(hql);
        query.setParameter("sender",sender);
        query.setParameter("receiver",receiver);
        query.executeUpdate();
    }


    public int deleteChat(MessageDto messageDto){
        String isSenderDeleted = messageDto.getIsSenderDeleted();
        String hql = getString(messageDto, isSenderDeleted);

        Query query = entityManager.createQuery(hql);
        query.setParameter("sender",messageDto.getSender());
        query.setParameter("receiver",messageDto.getReceiver());
        if(messageDto.getId() != null){
            query.setParameter("id",messageDto.getId());
        }else{
            query.setParameter("createdAt",messageDto.getCreatedAt());
        }
        return query.executeUpdate();
    }




    private static @NotNull String getString(MessageDto messageDto, String isSenderDeleted) {
        String isReceiverDeleted = messageDto.getIsReceiverDeleted();
        String hql = "update Chat set ";
        if(isSenderDeleted !=null && isReceiverDeleted != null){
            hql += " isSenderDeleted='H' , " +
                    "isReceiverDeleted= CASE " +
                    " WHEN isReceiverDeleted !='Y' THEN  'H'" +
                    " ELSE isReceiverDeleted " +
                    " END ";
        } else if (isSenderDeleted != null) {
            hql += " isSenderDeleted='"+ isSenderDeleted+"'";
        }else if (isReceiverDeleted != null) {
            hql += " isReceiverDeleted='"+isReceiverDeleted+"'";
        }

        hql += " where receiver=:receiver and sender=:sender ";
        if(messageDto.getId() !=null){
            hql+= " and id=:id";
        }else {
            hql+= " and createdAt=:createdAt";
        }
        return hql;
    }





}
