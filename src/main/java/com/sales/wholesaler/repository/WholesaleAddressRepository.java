package com.sales.wholesaler.repository;


import com.sales.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WholesaleAddressRepository extends JpaRepository<Address, Integer> {
    Address findAddressBySlug(String slug);
}
