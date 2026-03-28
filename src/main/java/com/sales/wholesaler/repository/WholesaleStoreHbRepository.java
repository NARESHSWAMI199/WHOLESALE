package com.sales.wholesaler.repository;

import com.sales.claims.AuthUser;
import com.sales.request.StoreCreationRequest;
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

    public int updateStore(StoreCreationRequest storeCreationRequest, AuthUser loggedUser){
        String strQuery = """ 
                update Store set 
                    storeName=:name,
                    email=:email,
                    avtar=:avtar,
                    phone=:phone,
                    storeCategory =:storeCategory,
                    storeSubCategory =:storeSubCategory,
                    description=:description,
                    updatedAt=:updatedAt,
                    updatedBy=:updatedBy
                where slug =:slug
            """;

        Query query = entityManager.createQuery(strQuery);
        query.setParameter("name", storeCreationRequest.getStoreName());
        query.setParameter("email", storeCreationRequest.getStoreEmail());
        query.setParameter("phone", storeCreationRequest.getStorePhone());
        query.setParameter("storeCategory", storeCreationRequest.getStoreCategory());
        query.setParameter("storeSubCategory", storeCreationRequest.getStoreSubCategory());
        query.setParameter("avtar", storeCreationRequest.getStoreAvatar());
        query.setParameter("description", storeCreationRequest.getDescription());
        query.setParameter("updatedAt", Utils.getCurrentMillis());
        query.setParameter("updatedBy", loggedUser.getId());
        query.setParameter("slug", storeCreationRequest.getStoreSlug());
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
