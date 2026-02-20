package com.sales.admin.repositories;


import com.sales.entities.Item;
import com.sales.entities.Item_;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository  extends JpaRepository<Item, Long> , JpaSpecificationExecutor<Item> {



   @Query("from Item where wholesaleId=:wholesaleId and createdAt >= :fromDate and createdAt <= :toDate")
   List<Item> getAllItemsWithFilters(@Param("wholesaleId")Integer wholesaleId,
                                            @Param("fromDate") Long fromDate,
                                            @Param("toDate") Long toDate);


   @EntityGraph(attributePaths = {Item_.ITEM_CATEGORY, Item_.ITEM_SUB_CATEGORY}, type = EntityGraph.EntityGraphType.FETCH)
   Item findItemBySlug(String slug);

   @Query(value = "select count(id) as count from Item")
   Integer totalItemCount();

   @Query(value = "select count(id) as count from Item where status=:status")
   Integer optionItemCount(@Param("status") String status);

   @Query(value = "select count(id) as count from Item where wholesaleId = :wholesaleId")
   Integer totalItemCountByWholesaleId(Integer wholesaleId);

}
