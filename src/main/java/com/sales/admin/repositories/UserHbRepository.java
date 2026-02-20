package com.sales.admin.repositories;


import com.sales.claims.AuthUser;
import com.sales.dto.UserRequest;
import com.sales.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor
public class UserHbRepository {

    private final EntityManager entityManager;

    public int updateUser(UserRequest userRequest, AuthUser loggedUser) {
        String strQuery = "update User set " +
                "username=:username , " +
                "email=:email,"+
                //"password=:password,"+
                "contact=:contact,"+
               // "userType=:userType,"+
                "updatedAt=:updatedAt,"+
                "updatedBy=:updatedBy "+
                "where slug =:slug";

        Query query = entityManager.createQuery(strQuery);
        query.setParameter("username", userRequest.getUsername());
        query.setParameter("email", userRequest.getEmail());
        //query.setParameter("password", userRequest.getPassword());
        query.setParameter("contact", userRequest.getContact());
        //query.setParameter("userType", userRequest.getUserType());
        query.setParameter("updatedAt", Utils.getCurrentMillis());
        query.setParameter("updatedBy", loggedUser.getId());
        query.setParameter("slug", userRequest.getSlug());
        return query.executeUpdate();
    }


    public int deleteUserBySlug(String slug){
        String hql = "Update User set isDeleted='Y' where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("slug",slug);
        return query.executeUpdate();
    }


    public int updateStatus(String slug, String status){
        String hql = "Update User set status=:status where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("status",status);
        query.setParameter("slug",slug);
        return query.executeUpdate();
    }

    public int updateProfileImage(String slug, String avatarPath){
        String hql = "Update User set avatar=:avatar where slug=:slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("avatar",avatarPath);
        query.setParameter("slug",slug);
        return query.executeUpdate();
    }


    public void updateOtp(String email, String otp){
        String hql = "Update User set otp=:otp where email=:email";
        Query query = entityManager.createQuery(hql);
        query.setParameter("otp",otp);
        query.setParameter("email",email);
        query.executeUpdate();
    }


}
