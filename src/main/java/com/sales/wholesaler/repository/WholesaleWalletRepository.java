package com.sales.wholesaler.repository;


import com.sales.entities.Wallet;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WholesaleWalletRepository extends JpaRepository<Wallet,Integer>, JpaSpecificationExecutor<Wallet> {

    @Transactional
    @Modifying
    @Query(value = "update Wallet set amount = amount + :amount, updatedAt = :updatedAt where userId = :userId")
    Integer addMoneyInWallet(@Param("amount") Float amount, @Param("userId") Integer userId, @Param("updatedAt") Long updatedAt);

    @Transactional
    @Modifying
    @Query(value = "update Wallet set amount = amount - :amount, updatedAt = :updatedAt where userId = :userId and amount >= :amount")
    Integer deductMoneyFromWallet(@Param("amount") Float amount,@Param("userId") Integer userId, @Param("updatedAt") Long updatedAt);

    Wallet findByUserId(Integer userId);

}
