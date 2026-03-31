package com.sales.admin.repositories;

import com.sales.entities.ItemReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemReportRepository extends JpaRepository<ItemReport, Long>, JpaSpecificationExecutor<ItemReport> {
}
