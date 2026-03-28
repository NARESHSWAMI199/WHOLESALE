package com.sales.wholesaler.repository;


import com.sales.entities.StorePermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WholsaleStorePermissionsRepository extends JpaRepository<StorePermissions, Long> {

    @Query("select id from StorePermissions where defaultPermission='Y'")
    List<Integer> getAllDefaultPermissionsIds();

}
