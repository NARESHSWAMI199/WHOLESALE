package com.sales.admin.repositories;


import com.sales.entities.WholesalerPlans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WholesalerPlansRepository extends JpaRepository<WholesalerPlans, Integer>, JpaSpecificationExecutor<WholesalerPlans> {
    WholesalerPlans findByUserId(Integer userId);
    /*
    @Query(value = "select " +
            "sp.name as name, " +
            "sp.price as price, " +
            "sp.discount as discount, " +
            "sp.months as months, " +
            "up.id as userPlanId, " +
            "up.slug as slug, "+
            "up.createdAt as createdAt, " +
            "up.expiryDate as expiryDate " +
            "from ServicePlan sp INNER JOIN WholesalerPlans up ON up.planId = sp.id where up.userId = :userId and :specification")
    List<Map<String,Object>> getAllUserPlansByUserId(Integer userId, Specification<WholesalerPlans> specification , Pageable pageable);
    */


}
