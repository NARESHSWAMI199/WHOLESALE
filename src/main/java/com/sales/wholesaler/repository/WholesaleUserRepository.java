package com.sales.wholesaler.repository;


import com.sales.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WholesaleUserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor {

    @Query(value = "from User where email=:email and password=:password and userType='W' ")
    User findByEmailAndPassword(@Param("email") String email, @Param("password") String password);


    @Query(value = "from User where slug=:slug and userType='W'")
    Optional<User> findByWholesalerSLug(@Param("slug") String slug);

    @Query(value = "from User where email=:email and userType='W' ")
    User findUserByEmail(@Param("email") String email);

    @Query(value = "from User where email=:email and otp=:otp and userType='W' ")
    User findUserByOtpAndEmail(@Param("email") String email, @Param("otp") String otp);

    @Query(value = "from User where slug=:slug and otp=:otp and userType='W' ")
    User findUserByOtpAndSlug(@Param("slug") String slug, @Param("otp") String otp);


    User findUserBySlug(String slug);

    @Query(value = "select count(id) as count from User")
    Integer totalUserCount();

    @Query(value = "select count(id) as count from User where status=:status")
    Integer optionUserCount(@Param("status") String status);


    @Query(value = "select count(id) as count from User where userType=:userType")
    Integer getUserWithUserType(@Param("userType") String userType);

    @Query(value = "select count(id) as count from User where status=:status and userType=:userType")
    Integer getUserWithUserType(@Param("status") String status, @Param("userType") String userType);

}
