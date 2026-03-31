package com.sales.wholesaler.repository;


import com.sales.entities.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WholesaleCategoryRepository extends JpaRepository<StoreCategory, Integer> {
}
