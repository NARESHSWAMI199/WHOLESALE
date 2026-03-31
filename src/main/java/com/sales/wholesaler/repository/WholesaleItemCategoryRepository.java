package com.sales.wholesaler.repository;


import com.sales.entities.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WholesaleItemCategoryRepository extends JpaRepository<ItemCategory, Integer> {
}
