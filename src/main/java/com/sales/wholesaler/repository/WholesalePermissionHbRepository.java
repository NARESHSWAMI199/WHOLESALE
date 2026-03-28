package com.sales.wholesaler.repository;


import com.sales.exceptions.MyException;
import com.sales.global.GlobalConstant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
@Slf4j
@RequiredArgsConstructor
public class WholesalePermissionHbRepository {

    private final EntityManager entityManager;

    /**
     * permissions for wholesaler
     */
    public void deleteWholesalerPermission(int userId) {
        if (userId == GlobalConstant.suId) return;
        String sql = "delete from `wholesaler_permissions` where user_id = :userId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        query.executeUpdate();
    }

    public int assignPermissionsToWholesaler(int userId, List<Integer> permissions) throws MyException {
        if (permissions.contains(GlobalConstant.suId)) permissions.remove((Integer) GlobalConstant.suId);
        deleteWholesalerPermission(userId);
        if (permissions.isEmpty()) throw new MyException("Please provide at least one permission.");
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < permissions.size(); i++) {
            values.append("(").append(userId).append(",").append(permissions.get(i)).append(")");
            if (i < permissions.size() - 1) values.append(",");
        }
        String sql = "insert into wholesaler_permissions (user_id,permission_id) values " + values;
        Query query = entityManager.createNativeQuery(sql);
        return query.executeUpdate();
    }

}
