package com.sales.admin.repositories;

import com.sales.entities.Store;
import com.sales.entities.Store_;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> , JpaSpecificationExecutor<Store> {

    @EntityGraph(attributePaths = {Store_.USER, Store_.ADDRESS, Store_.STORE_CATEGORY, Store_.STORE_SUB_CATEGORY}, type = EntityGraph.EntityGraphType.FETCH)
    Store findStoreBySlug(String slug);

    @EntityGraph(attributePaths = {Store_.USER, Store_.ADDRESS, Store_.STORE_CATEGORY, Store_.STORE_SUB_CATEGORY}, type = EntityGraph.EntityGraphType.FETCH)
    Store findStoreByUserId(int userId);

    @Query(value = "select count(id) as count from Store")
    Integer totalWholesaleCount();
    @Query(value = "select count(id) as count from Store where status=:status")
    Integer optionWholesaleCount(@Param("status") String status);

    // native query
    @Query(value = "SELECT count(id) from stores s where FROM_UNIXTIME(created_at /1000,'%m') =:month and FROM_UNIXTIME(created_at /1000,'%Y') =:year and is_deleted='N'",nativeQuery = true)
    Integer totalStoreViaMonth(@Param("month") Integer month,@Param("year") Integer year);

    @Query("SELECT a.id FROM Store s JOIN s.address a WHERE s.slug = :slug")
    Integer getAddressIdBySlug(String slug);

    @Query(value = "select id from Store where slug=:slug")
    Integer getStoreIdByStoreSlug(String slug);


    @Query(value = "select id as id from Store where user.id=:userId")
    Integer getStoreIdByUserId(@Param("userId")Integer userId);

    @Query("SELECT slug as slug From Store WHERE id=:storeId")
    String findStoreSlugByStoreId(@Param("storeId") Integer storeId);

}
