package com.sales.wholesaler.repository;

import com.sales.claims.AuthUser;
import com.sales.request.StoreRequest;
import com.sales.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor
public class WholesaleStoreHbRepository {

    private final EntityManager entityManager;

    public int updateStore(StoreRequest storeRequest, AuthUser loggedUser){
        String strQuery = "update Store set " +
                "storeName=:name , " +
                "email=:email, "+
                "avtar=:avtar, "+
                "phone=:phone, "+
                "storeCategory =:storeCategory,"+
                "storeSubCategory =:storeSubCategory,"+
                "description=:description, "+
                "updatedAt=:updatedAt, "+
                "updatedBy=:updatedBy "+
                "where slug =:slug";

        Query query = entityManager.createQuery(strQuery);
        query.setParameter("name", storeRequest.getStoreName());
        query.setParameter("email", storeRequest.getStoreEmail());
        query.setParameter("phone", storeRequest.getStorePhone());
        query.setParameter("storeCategory", storeRequest.getStoreCategory());
        query.setParameter("storeSubCategory", storeRequest.getStoreSubCategory());
        query.setParameter("avtar", storeRequest.getStoreAvatar());
        query.setParameter("description", storeRequest.getDescription());
        query.setParameter("updatedAt", Utils.getCurrentMillis());
        query.setParameter("updatedBy", loggedUser.getId());
        query.setParameter("slug", storeRequest.getStoreSlug());
        return query.executeUpdate();
    }



    public int updateSeenNotifications(long id){
        String strQuery = "update StoreNotifications set " +
                "seen='Y' " +
                "where id=:id";
        Query query = entityManager.createQuery(strQuery);
        query.setParameter("id", id);
        return query.executeUpdate();
    }


}
