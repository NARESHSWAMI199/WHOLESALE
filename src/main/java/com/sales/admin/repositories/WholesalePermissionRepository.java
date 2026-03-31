package com.sales.admin.repositories;

import com.sales.entities.WholesalerPermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WholesalePermissionRepository extends JpaRepository<WholesalerPermissions, Integer> {
}
