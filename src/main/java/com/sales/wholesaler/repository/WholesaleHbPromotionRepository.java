package com.sales.wholesaler.repository;


import com.sales.claims.AuthUser;
import com.sales.dto.StorePromotionDto;
import com.sales.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor
public class WholesaleHbPromotionRepository {

    private final EntityManager entityManager;


    public int insertStorePromotions(StorePromotionDto storePromotionDto, AuthUser loggedUser){
        String hql = "INSERT INTO store_promotions " +
                        "(banner_img, promotion_type, store_id, item_id, priority, priority_hours, max_repeat, state, city, created_date, start_date, expiry_date, created_by, is_deleted) " +
                        "VALUES (:bannerImage,:promotionType,:storeId,:itemId,:priority,:priorityHours,:maxRepeat,:stateId,:cityId,:createdDate,:startDate,:expiryDate, :createdBy,'N')";
        Query query = entityManager.createNativeQuery(hql);
        query.setParameter("bannerImage",storePromotionDto.getBannerImage())
                .setParameter("promotionType", storePromotionDto.getPromotionType())
                .setParameter("storeId",storePromotionDto.getStoreId())
                .setParameter("itemId",storePromotionDto.getItemId())
                .setParameter("priority",storePromotionDto.getPriority())
                .setParameter("priorityHours",storePromotionDto.getPriorityHours())
                .setParameter("maxRepeat",storePromotionDto.getMaxRepeat())
                .setParameter("stateId",storePromotionDto.getStateId())
                .setParameter("cityId",storePromotionDto.getCityId())
                .setParameter("createdDate", Utils.getCurrentMillis())
                .setParameter("startDate",storePromotionDto.getExpiryDate())
                .setParameter("expiryDate", storePromotionDto.getExpiryDate())
                .setParameter("createdBy", loggedUser.getId());
        return query.executeUpdate();
    }

}
