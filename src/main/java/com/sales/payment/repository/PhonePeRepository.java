package com.sales.payment.repository;


import com.sales.entities.PhonePeTrans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PhonePeRepository extends JpaRepository<PhonePeTrans, Integer>, JpaSpecificationExecutor<PhonePeTrans> {
}
