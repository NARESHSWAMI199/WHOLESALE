package com.sales.admin.repositories;

import com.sales.entities.ServicePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicePlanRepository extends JpaRepository<ServicePlan, Integer>, JpaSpecificationExecutor<ServicePlan> {

    ServicePlan findBySlug(@Param("slug") String slug);

}
