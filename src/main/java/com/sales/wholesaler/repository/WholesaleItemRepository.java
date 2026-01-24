package com.sales.wholesaler.repository;


import com.sales.entities.Item;
import com.sales.entities.Item_;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WholesaleItemRepository extends JpaRepository<Item, Integer> , JpaSpecificationExecutor<Item> {

   @EntityGraph(attributePaths = {Item_.ITEM_CATEGORY,Item_.ITEM_SUB_CATEGORY})
   Item findItemBySlug(String slug);


   @Query(value = "select status from Item where slug=:slug")
   String getItemStatus(String slug);

   @Query(value = "select count(id) as count from Item where wholesaleId=:id")
   Integer totalItemCount(@Param("id") Integer storeId);

   @Query(value = "select count(id) as count from Item where wholesaleId=:id and status=:status")
   Integer optionItemCount(@Param("status") String status,@Param("id") Integer storeId);

   @Query(value = "select count(id) as count from Item where wholesaleId=:id and label=:label")
   Integer getItemCountLabel(@Param("label") String label,@Param("id") Integer storeId);

   @Query(value = "select count(id) as count from Item where wholesaleId=:id and label=:label and status=:status")
   Integer optionItemCountLabel(@Param("label") String label,@Param("status") String status,@Param("id") Integer storeId);

   @Query(value = "select count(id) as count from Item where wholesaleId=:id and inStock=:inStock")
   Integer getItemCountInStock(@Param("inStock") String inStock,@Param("id") Integer storeId);


   @Query(value = "select count(id) as count from Item where wholesaleId=:id  and label=:label and inStock=:inStock")
   Integer getItemCountInStock(@Param("inStock") String inStock,@Param("label") String label,@Param("id") Integer storeId);


   @Query(value = "select count(id) as count from Item where wholesaleId=:id and inStock=:inStock and status=:status")
   Integer optionItemCountInStock(@Param("inStock") String inStock,@Param("status") String status,@Param("id") Integer storeId);


//   @Query(value = "SELECT count(id) from item s where wholesale_id=:storeId and  FROM_UNIXTIME(created_at /1000,'%m') =:month and FROM_UNIXTIME(created_at /1000,'%Y') =:year and is_deleted='N'",nativeQuery = true)
//   Integer totalItemsViaMonth(@Param("month") Integer month, @Param("year") Integer year, @Param("storeId")Integer storeId);

   // Improved
   @Query(value = """
    SELECT count(id) as count
    FROM Item
    WHERE wholesaleId = :storeId
      AND isDeleted = 'N'
      AND createdAt >= :startOfMonth
      AND createdAt < :startOfNextMonth
   """)
   Integer totalItemsViaMonth(
           @Param("storeId") Integer storeId,
           @Param("startOfMonth") Long startOfMonth,
           @Param("startOfNextMonth") Long startOfNextMonth
   );


}
