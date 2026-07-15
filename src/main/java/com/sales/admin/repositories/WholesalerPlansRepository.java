package com.sales.admin.repositories;


import com.sales.admin.dto.MonthlyRevenueProjection;
import com.sales.entities.WholesalerPlans;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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

    // Highest sold plans
    @Query("""
            SELECT
                wp.servicePlan as servicePlan,
                COUNT(wp.id) as soldCount
            FROM
                WholesalerPlans wp
            WHERE
                wp.servicePlan IS NOT NULL
            GROUP BY
                wp.servicePlan.id
            """)
    List<Map<String, Object>> getMostSoldPlans();

    @Query("""
            SELECT SUM(wp.servicePlan.price)
            FROM WholesalerPlans wp 
            WHERE wp.createdAt >= :startDate
            """)
    Long currentMonthRevenue(@Param("startDate") Long startDate);

    @Query(value = """
                SELECT 
                    MONTH(FROM_UNIXTIME(wp.created_at / 1000)) as month, 
                    SUM(sp.price) as totalRevenue 
                FROM wholesaler_plans wp
                JOIN service_plans sp ON wp.plan_id = sp.id
                WHERE YEAR(FROM_UNIXTIME(wp.created_at / 1000)) = :fullYear
                GROUP BY month
                ORDER BY month ASC
            """, nativeQuery = true)
    List<MonthlyRevenueProjection> getYearlyRevenueByMonth(@Param("fullYear") int fullYear);


}
