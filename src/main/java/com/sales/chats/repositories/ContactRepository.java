package com.sales.chats.repositories;


import com.sales.claims.AuthUser;
import com.sales.entities.Contact;
import com.sales.entities.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact,Integer> {

    @Query("select contactUser from Contact where userId=:userId")
    List<User> getContactByUserId(Integer userId);


    @Modifying
    @Transactional
    @Query("delete from Contact where userId=:userId and contactUser=:contactUser")
    Integer deleteContactUserFromContact(Integer userId, AuthUser contactUser);

}
