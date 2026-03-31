package com.sales.wholesaler.repository;


import com.sales.entities.StoreSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WholesaleSubCategoryRepository extends JpaRepository<StoreSubCategory, Integer> {
    @Query(value = "from StoreSubCategory ssc where ssc.categoryId =:categoryId or ssc.categoryId=-1 order by ssc.categoryId asc")
    List<StoreSubCategory> getSubCategories(@Param("categoryId") int categoryId);

}
