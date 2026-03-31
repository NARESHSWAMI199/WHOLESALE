package com.sales.admin.repositories;


import com.sales.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer>, JpaSpecificationExecutor<Wallet> {

    @Transactional
    @Modifying
    @Query(value = "update Wallet set amount = amount + :amount, updatedAt = :updatedAt where userId = :userId")
    Integer addMoneyInWallet(@Param("amount") Float amount, @Param("userId") Integer userId, @Param("updatedAt") Long updatedAt);

    @Transactional
    @Modifying
    @Query(value = "update Wallet set amount = amount - :amount, updatedAt = :updatedAt where userId = :userId and amount >= :amount")
    Integer deductMoneyFromWallet(@Param("amount") Float amount, @Param("userId") Integer userId, @Param("updatedAt") Long updatedAt);

    Wallet findByUserId(Integer userId);

}
