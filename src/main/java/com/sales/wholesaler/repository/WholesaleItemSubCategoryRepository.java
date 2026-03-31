package com.sales.wholesaler.repository;


import com.sales.entities.ItemSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WholesaleItemSubCategoryRepository extends JpaRepository<ItemSubCategory, Integer> {

    @Query(value = "from ItemSubCategory isc where isc.categoryId =:categoryId or isc.categoryId=-1 order by subcategory asc")
    List<ItemSubCategory> getSubCategories(@Param("categoryId") int categoryId);

}
