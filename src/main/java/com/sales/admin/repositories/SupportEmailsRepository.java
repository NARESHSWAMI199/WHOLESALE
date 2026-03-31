package com.sales.admin.repositories;


import com.sales.entities.SupportEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportEmailsRepository extends JpaRepository<SupportEmail, Integer> {

    SupportEmail findSupportEmailBySupportType(String supportType);

}
