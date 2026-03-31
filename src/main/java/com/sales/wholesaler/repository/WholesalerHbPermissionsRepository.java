package com.sales.wholesaler.repository;


import com.sales.exceptions.MyException;
import com.sales.global.GlobalConstant;
import com.sales.global.ResponseMessages;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
@RequiredArgsConstructor
public class WholesalerHbPermissionsRepository {


    private final Logger logger = LoggerFactory.getLogger(WholesalerHbPermissionsRepository.class);

    private final EntityManager entityManager;


    public int deleteWholesalerPermission(int userId) {
        if (userId == GlobalConstant.suId) return 0;
        String sql = "delete from `user_groups` where user_id = :userId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        return query.executeUpdate();
    }

    public int assignPermissionsToWholesaler(int userId, List<Integer> permissions) throws MyException {
        if (permissions.contains(GlobalConstant.suId)) permissions.remove((Integer) GlobalConstant.suId);
        deleteWholesalerPermission(userId);
        if (permissions.isEmpty()) throw new MyException(ResponseMessages.PLEASE_PROVIDE_AT_LEAST_ONE_PERMISSION);
        String values = "";
        for (int i = 0; i < permissions.size(); i++) {
            values += "(" + userId + "," + permissions.get(i) + ")";
            if (i < permissions.size() - 1) values += ",";
        }
        String sql = "insert into wholesaler_permissions (user_id,permission_id) values " + values;
        Query query = entityManager.createNativeQuery(sql);
        return query.executeUpdate();
    }

}
