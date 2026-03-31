package com.sales.wholesaler.repository;


import com.sales.entities.WholesalerPlans;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WholesaleUserPlansRepository extends JpaRepository<WholesalerPlans, Integer>, JpaSpecificationExecutor<WholesalerPlans> {

    WholesalerPlans findByUserId(Integer userId);

    default WholesalerPlans findLastPlanByUserId(Integer userId, EntityManager entityManager) {
        jakarta.persistence.Query query = entityManager.createQuery("from WholesalerPlans where userId = :userId order by createdAt desc");
        query.setParameter("userId", userId);
        query.setMaxResults(1); // Getting only one result something like limit 1 in SQL query.
        List<WholesalerPlans> resultList = query.getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @Query(value = "select id from WholesalerPlans where userId=:userId and slug=:slug")
    Integer getWholesaleUserPlanId(Integer userId, String slug);




   /*
   @Query(value = "select " +
           "sp.name as name, " +
           "sp.price as price, " +
           "sp.discount as discount, " +
           "sp.months as months, " +
           "up.id as userPlanId, " +
           "up.createdAt as createdAt, " +
           "up.expiryDate as expiryDate " +
           "from ServicePlan sp INNER JOIN WholesalerPlans up ON up.planId = sp.id where up.userId = :userId order by up.id desc")
   List<Map<String,Object>> getAllUserPlansByUserId(Integer userId);
    */


}
