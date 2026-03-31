package com.sales.chats.repositories;


import com.sales.claims.AuthUser;
import com.sales.entities.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockListRepository extends JpaRepository<BlockedUser, Integer> {
    BlockedUser findByUserIdAndBlockedUser(Integer userId, AuthUser blockedUser);
}
