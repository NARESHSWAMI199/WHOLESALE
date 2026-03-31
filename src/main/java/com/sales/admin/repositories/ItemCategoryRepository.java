package com.sales.admin.repositories;


import com.sales.entities.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Integer> {

    @Query(value = "select id from ItemCategory where slug =:slug")
    Integer getItemCategoryIdBySlug(@Param("slug") String slug);

}
