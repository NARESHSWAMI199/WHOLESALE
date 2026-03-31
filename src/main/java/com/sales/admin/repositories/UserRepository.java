package com.sales.admin.repositories;


import com.sales.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    @Query(value = "from User where email=:email and password=:password and (userType='S' or userType='SA') ")
    Optional<User> findByEmailAndPassword(@Param("email") String email, @Param("password") String password);


    @Query(value = "from User where slug=:slug and userType='W'")
    Optional<User> findByWholesalerSlug(@Param("slug") String slug);

    User findUserBySlug(String slug);

    @Query(value = "from User where email=:email and otp=:otp and (userType='S' or userType='SA') ")
    User findUserByOtpAndEmail(@Param("email") String email, @Param("otp") String otp);

    @Query(value = "from User where email=:email and (userType='S' or userType='SA')   ")
    Optional<User> findUserByEmail(@Param("email") String email);

    @Query("select id from User where slug=:slug")
    Integer getUserIdBySlug(String slug);

    @Query(value = "select count(id) as count from User where userType !='SA'")
    Integer totalUserCount();

    @Query(value = "select count(id) as count from User where status=:status")
    Integer optionUserCount(@Param("status") String status);


    @Query(value = "select count(id) as count from User where userType=:userType")
    Integer getUserWithUserType(@Param("userType") String userType);

    @Query(value = "select count(id) as count from User where status=:status and userType=:userType")
    Integer getUserWithUserType(@Param("status") String status, @Param("userType") String userType);

//
//    @Query(value = "select ug.groupId as groupId from UserGroups ug left join User u on ug.userId = u.id where u.slug = :slug")
//    List<Integer> getUserGroupsIdBySlug(String slug);

    @Query("""
                select ug.id as id
                from User u join u.groups ug
                where u.slug = :slug
            """)
    List<Integer> findGroupIdsBySlug(String slug);


    @Query("""
            SELECT p.permission 
            FROM User u
            JOIN u.groups ug
            JOIN ug.permissions p
            WHERE u.id = :userId
            """)
    Set<String> findAllPermissionsByUserId(@Param("userId") Integer userId);

    Optional<User> findByEmail(String email);

}
