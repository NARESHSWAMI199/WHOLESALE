package com.sales.admin.repositories;


import com.sales.entities.StoreReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreReportRepository extends JpaRepository<StoreReport, Long>, JpaSpecificationExecutor<StoreReport> {

}
