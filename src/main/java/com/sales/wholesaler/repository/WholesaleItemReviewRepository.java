package com.sales.wholesaler.repository;


import com.sales.entities.ItemReviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WholesaleItemReviewRepository extends JpaRepository<ItemReviews, Long>, JpaSpecificationExecutor<ItemReviews> {

    @Query("select count(id) from ItemReviews where parentId=:parentId")
    Integer totalReplies(@Param("parentId") Integer parentId);
}
