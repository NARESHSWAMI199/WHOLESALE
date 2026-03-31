package com.sales.wholesaler.repository;


import com.sales.claims.AuthUser;
import com.sales.request.StorePromotionRequest;
import com.sales.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class WholesaleHbPromotionRepository {

    private final EntityManager entityManager;


    public int insertStorePromotions(StorePromotionRequest storePromotionRequest, AuthUser loggedUser) {
        String hql = "INSERT INTO store_promotions " +
                "(banner_img, promotion_type, store_id, item_id, priority, priority_hours, max_repeat, state, city, created_date, start_date, expiry_date, created_by, is_deleted) " +
                "VALUES (:bannerImage,:promotionType,:storeId,:itemId,:priority,:priorityHours,:maxRepeat,:stateId,:cityId,:createdDate,:startDate,:expiryDate, :createdBy,'N')";
        Query query = entityManager.createNativeQuery(hql);
        query.setParameter("bannerImage", storePromotionRequest.getBannerImage())
                .setParameter("promotionType", storePromotionRequest.getPromotionType())
                .setParameter("storeId", storePromotionRequest.getStoreId())
                .setParameter("itemId", storePromotionRequest.getItemId())
                .setParameter("priority", storePromotionRequest.getPriority())
                .setParameter("priorityHours", storePromotionRequest.getPriorityHours())
                .setParameter("maxRepeat", storePromotionRequest.getMaxRepeat())
                .setParameter("stateId", storePromotionRequest.getStateId())
                .setParameter("cityId", storePromotionRequest.getCityId())
                .setParameter("createdDate", Utils.getCurrentMillis())
                .setParameter("startDate", storePromotionRequest.getExpiryDate())
                .setParameter("expiryDate", storePromotionRequest.getExpiryDate())
                .setParameter("createdBy", loggedUser.getId());
        return query.executeUpdate();
    }

}
