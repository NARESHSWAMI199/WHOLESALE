package com.sales.wholesaler.repository;

import com.sales.claims.AuthUser;
import com.sales.exceptions.MyException;
import com.sales.request.MessageDto;
import com.sales.request.UserRequest;
import com.sales.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = {IllegalArgumentException.class, MyException.class, RuntimeException.class})
@RequiredArgsConstructor
public class WholesaleUserHbRepository {

    private final EntityManager entityManager;

    public int updateUser(UserRequest userRequest, AuthUser loggedUser) {
        String strQuery = "update User set " +
                "username=:username , " +
                "email=:email," +
                "contact=:contact," +
                "updatedAt=:updatedAt," +
                "updatedBy=:updatedBy " +
                "where slug =:slug";

        Query query = entityManager.createQuery(strQuery);
        query.setParameter("username", userRequest.getUsername());
        query.setParameter("email", userRequest.getEmail());
        query.setParameter("contact", userRequest.getContact());
        query.setParameter("updatedAt", Utils.getCurrentMillis());
        query.setParameter("updatedBy", loggedUser.getId());
        query.setParameter("slug", userRequest.getSlug());
        return query.executeUpdate();
    }


    public int deleteUserBySlug(String slug) {
        String hql = "Update User set isDeleted='Y' where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("slug", slug);
        return query.executeUpdate();
    }


    public int updateStatus(String slug, String status) {
        String hql = "Update User set status=:status where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("status", status);
        query.setParameter("slug", slug);
        return query.executeUpdate();
    }


    public int updateProfileImage(String slug, String avatarPath) {
        String hql = "Update User set avatar=:avatar where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("avatar", avatarPath);
        query.setParameter("slug", slug);
        return query.executeUpdate();
    }

    public void updateOtp(String email, String otp) {
        String hql = "Update User set otp=:otp where email=:email";
        Query query = entityManager.createQuery(hql);
        query.setParameter("otp", otp);
        query.setParameter("email", email);
        query.executeUpdate();
    }

    public int makeUserTypeWholesaler(String slug) {
        String hql = "update User set userType ='W' where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("slug", slug);
        return query.executeUpdate();
    }


    public int updateUserActivePlan(Integer id, Integer userPlanId) {
        String hql = "update User set activePlan =:userPlanId where id=:id";
        Query query = entityManager.createQuery(hql);
        query.setParameter("userPlanId", userPlanId);
        query.setParameter("id", id);
        return query.executeUpdate();
    }


    public int updatedUserLastSeen(String slug) {
        String hql = "update User set lastSeen=:lastSeen where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("lastSeen", Utils.getCurrentMillis());
        query.setParameter("slug", slug);
        return query.executeUpdate();
    }

    public boolean updateSeenMessage(MessageDto message) {
        String hql = "update Chat set seen=true where sender =:sender and receiver = :receiver and isSent ='S'";
        Query query = entityManager.createQuery(hql);
        query.setParameter("sender", message.getSender());
        query.setParameter("receiver", message.getReceiver());
        return query.executeUpdate() > 0;
    }

}
