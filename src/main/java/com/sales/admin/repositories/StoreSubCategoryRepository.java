package com.sales.admin.repositories;


import com.sales.entities.StoreSubCategory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreSubCategoryRepository extends JpaRepository<StoreSubCategory, Integer> {
    @Query(value = "from StoreSubCategory ssc where ssc.categoryId =:categoryId or ssc.categoryId=-1")
    List<StoreSubCategory> getSubCategories(@Param("categoryId") int categoryId, Sort sort);

    @Query(value = "select id from StoreSubCategory ssc where ssc.slug =:slug")
    Integer getStoreSubCategoryIdBySlug(@Param("slug") String slug);

}
