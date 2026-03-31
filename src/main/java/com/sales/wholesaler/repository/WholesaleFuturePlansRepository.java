package com.sales.wholesaler.repository;

import com.sales.entities.WholesalerFuturePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Repository
public interface WholesaleFuturePlansRepository extends JpaRepository<WholesalerFuturePlan, Long>, JpaSpecificationExecutor<WholesalerFuturePlan> {

    Page<WholesalerFuturePlan> findWholesalerFuturePlansByUserIdAndStatus(Pageable pageable, Integer userId, String status);

    @Query(value = """
                select 
                    wfp.id as wholesalerFuturePlanId,
                    sp.id as servicePlanId
                from WholesalerFuturePlan wfp 
                    JOIN ServicePlan sp ON sp.id = wfp.servicePlan.id 
                where wfp.slug=:slug and wfp.userId=:userId and wfp.status = 'N'
            """)
    Map<String, Object> getNewFuturePlanByUserIdAndSlug(String slug, Integer userId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE WholesalerFuturePlan set status = 'O', updatedAt =:updatedAt where id = :id ")
    int updateWholesalerFuturePlans(@Param("id") Long id, Long updatedAt);

}
