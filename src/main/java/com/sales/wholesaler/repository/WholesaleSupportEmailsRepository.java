package com.sales.wholesaler.repository;


import com.sales.entities.SupportEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WholesaleSupportEmailsRepository extends JpaRepository<SupportEmail, Integer> {

    SupportEmail findSupportEmailBySupportType(String supportType);

}
