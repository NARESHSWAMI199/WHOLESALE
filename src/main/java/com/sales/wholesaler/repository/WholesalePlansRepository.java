package com.sales.wholesaler.repository;

import com.sales.entities.WholesalerPlans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WholesalePlansRepository extends JpaRepository<WholesalerPlans, Integer> {
}
