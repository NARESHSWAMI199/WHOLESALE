package com.sales.admin.repositories;


import com.sales.claims.AuthUser;
import com.sales.dto.GroupRequest;
import com.sales.exceptions.MyException;
import com.sales.global.GlobalConstant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PermissionHbRepository {

    private final EntityManager entityManager;


    public int updateGroup(GroupRequest groupRequest,int groupId,boolean isSuperAdmin){
        // deleting all group's exists permissions
        deleteGroupPermissionByGroupId(groupId,isSuperAdmin);

        String hql = "update Group set name=:name where slug = :slug";
        Query query = entityManager.createQuery(hql);
        query.setParameter("name", groupRequest.getName());
        query.setParameter("slug",groupRequest.getSlug());

        // update permissions if there provided
        List<Integer> permissions = groupRequest.getPermissions();
        if(permissions !=null && !permissions.isEmpty()) updatePermissions(groupId,permissions);
        return query.executeUpdate();
    }


    public int updatePermissions(int groupId, List<Integer> permissions){
        if(permissions ==null || permissions.isEmpty()) return 0;
        String values = "";
        for(int i=0; i < permissions.size(); i++){
            values +="("+groupId+","+permissions.get(i)+")";
            if(i < permissions.size()-1) values += ",";
        }
        String sql = "insert into group_permissions (group_id,permission_id) values "+values;
        Query query = entityManager.createNativeQuery(sql);
        return query.executeUpdate();
    }


    public int deleteGroupBySlug(String slug, int groupId,boolean isSuperAdmin){
        if (groupId == GlobalConstant.groupId) throw new PermissionDeniedDataAccessException("We can't delete this group.",new Exception());
        deleteGroupPermissionByGroupId(groupId,isSuperAdmin);
        deleteGroupFromUser(groupId);
        String sql = "delete from Group where slug=:slug";
        Query query = entityManager.createQuery(sql);
        query.setParameter("slug",slug);
        return query.executeUpdate();
    }

    public int deleteGroupPermissionByGroupId(int groupId,boolean isSuperAdmin){
        // If group is a super group do nothing.
        if (groupId == GlobalConstant.groupId && !isSuperAdmin) return  0;
        String sql = "delete from group_permissions where group_id = :groupId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("groupId",groupId);
        return query.executeUpdate();
    }

    public int deleteGroupFromUser(int groupId){
        // If group is a super group do nothing.
        if (groupId == GlobalConstant.groupId) return  0;
        String sql = "delete from `user_groups` where group_id = :groupId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("groupId",groupId);
        return query.executeUpdate();
    }


    public int deleteUserGroups(int userId){
        // If user is a super admin do nothing.
        if (userId == GlobalConstant.suId) return  0;
        String sql = "delete from `user_groups` where user_id = :userId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId",userId);
        return query.executeUpdate();
    }



    public int assignGroupsToUser(int userId, List<Integer> groups, AuthUser loggedUser) throws MyException {
        if(groups.contains(GlobalConstant.groupId) && loggedUser.getId() != GlobalConstant.suId) groups.remove((Integer) GlobalConstant.groupId);
        deleteUserGroups(userId);
        if(groups.isEmpty()) throw new MyException("Please provide at least one group.");
        StringBuilder values = new StringBuilder();
        for(int i=0; i < groups.size(); i++){
            values.append("(").append(userId).append(",").append(groups.get(i)).append(")");
            if(i < groups.size()-1) values.append(",");
        }
        String sql = "insert into user_groups (user_id,group_id) values "+values;
        Query query = entityManager.createNativeQuery(sql);
        return query.executeUpdate();
    }



    /** permissions for wholesaler */
    public int deleteWholesalerPermission(int userId){
        if (userId == GlobalConstant.suId) return  0;
        String sql = "delete from `wholesaler_permissions` where user_id = :userId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId",userId);
        return query.executeUpdate();
    }

    public int assignPermissionsToWholesaler(int userId, List<Integer> permissions) throws MyException {
        if(permissions.contains(GlobalConstant.suId)) permissions.remove((Integer) GlobalConstant.suId);
        deleteWholesalerPermission(userId);
        if(permissions.isEmpty()) throw new MyException("Please provide at least one permission.");
        StringBuilder values = new StringBuilder();
        for(int i=0; i < permissions.size(); i++){
            values.append("(").append(userId).append(",").append(permissions.get(i)).append(")");
            if(i < permissions.size()-1) values.append(",");
        }
        String sql = "insert into wholesaler_permissions (user_id,permission_id) values "+values;
        Query query = entityManager.createNativeQuery(sql);
        return query.executeUpdate();
    }

}
