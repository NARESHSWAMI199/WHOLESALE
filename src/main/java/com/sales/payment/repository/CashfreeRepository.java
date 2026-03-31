package com.sales.payment.repository;

import com.sales.entities.CashfreeTrans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CashfreeRepository extends JpaRepository<CashfreeTrans, Long>, JpaSpecificationExecutor<CashfreeTrans> {

}
