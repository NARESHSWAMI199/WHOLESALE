package com.sales.wholesaler.repository;

import com.sales.entities.ServicePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WholesaleServicePlanRepository extends JpaRepository<ServicePlan, Integer>, JpaSpecificationExecutor<ServicePlan> {

    ServicePlan findBySlug(@Param("slug") String slug);

    @Query("from ServicePlan where price = 0")
    ServicePlan getDefaultServicePlan();

}
