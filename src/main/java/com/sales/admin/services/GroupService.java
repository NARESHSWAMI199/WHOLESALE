package com.sales.admin.services;


import com.sales.admin.dto.GroupDto;
import com.sales.admin.mapper.GroupMapper;
import com.sales.admin.repositories.GroupRepository;
import com.sales.admin.repositories.PermissionHbRepository;
import com.sales.admin.repositories.PermissionRepository;
import com.sales.cachemanager.services.UserCacheService;
import com.sales.claims.AuthUser;
import com.sales.request.DeleteRequest;
import com.sales.request.GroupRequest;
import com.sales.request.SearchFilters;
import com.sales.request.UserPermissionsRequest;
import com.sales.entities.Group;
import com.sales.entities.Permission;
import com.sales.exceptions.NotFoundException;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import com.sales.global.USER_TYPES;
import com.sales.utils.Utils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sales.helpers.PaginationHelper.getPageable;
import static com.sales.specifications.GroupSpecifications.*;

@Service
@RequiredArgsConstructor
public class GroupService {


    private final GroupRepository groupRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionHbRepository permissionHbRepository;
    private final UserCacheService userCacheService;
    private final GroupMapper groupMapper;
    
    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

    @Transactional(readOnly = true)
    public Page<GroupDto> getAllGroups(SearchFilters filters, AuthUser loggedUser) {
        logger.debug("Entering getAllGroups with filters: {}, loggedUser: {}", filters, loggedUser);
        Specification<Group> specification = Specification.allOf(
                (containsName(filters.getSearchKey()))
                        .and(greaterThanOrEqualFromDate(filters.getFromDate()))
                        .and(lessThanOrEqualToToDate(filters.getToDate()))
                        .and(hasSlug(filters.getSlug()))
                        .and(notSuperAdmin(loggedUser))
        );
        Pageable pageable = getPageable(logger,filters);
        Page<Group> result = groupRepository.findAll(specification, pageable);
        logger.debug("Exiting getAllGroups with result: {}", result);
        return result.map(groupMapper::toDto);
    }

    public void validateRequiredFieldsForGroup(GroupRequest groupRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering validateRequiredFieldsForGroup with groupRequest: {}", groupRequest);
        List<String> requiredFields = new ArrayList<>(List.of("name"));
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(groupRequest, requiredFields);
        logger.debug("Exiting validateRequiredFieldsForGroup");
    }

    @Transactional(rollbackFor = {IllegalArgumentException.class, NotFoundException.class, RuntimeException.class, Exception.class})
    public Map<String, Object> createOrUpdateGroup(GroupRequest groupRequest, AuthUser loggedUser, String path) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering createOrUpdateGroup with groupRequest: {}, loggedUser: {}, path: {}", groupRequest, loggedUser, path);
        Map<String, Object> responseObject = new HashMap<>();

        // Validating the required fields if there is any required field is null then this is throw Exception
        validateRequiredFieldsForGroup(groupRequest);

        //Only super admin can create or update a group.
        if(!loggedUser.getUserType().equals(USER_TYPES.SUPER_ADMIN.getType())) throw new PermissionDeniedDataAccessException("You don't have permission to create or update a group. Please contact a super admin",new Exception());

        if (!Utils.isEmpty(groupRequest.getSlug()) || path.contains("update")) {
            logger.debug("We are going to update the group.");
            // if there is any required field null then this will throw IllegalArgumentException
            Utils.checkRequiredFields(groupRequest, List.of("slug"));

            Group group = groupRepository.findGroupBySlug(groupRequest.getSlug());
            if (group == null) throw new NotFoundException("No group found to update.");
            if(group.getId() == GlobalConstant.groupId && loggedUser.getId() != GlobalConstant.suId) throw  new NotFoundException("There is nothing to update.");

            // Going to update existing group.
            int isUpdated = permissionHbRepository.updateGroup(groupRequest, group.getId(),loggedUser.getId() == GlobalConstant.suId);
            if (isUpdated > 0 && group.getId() == GlobalConstant.groupId ) {
                responseObject.put(ConstantResponseKeys.MESSAGE, "The group has been updated successfully. But dear " + loggedUser.getUsername() + " ji We are not able to remove permissions. from " + group.getName() + " New permissions updated.");
                responseObject.put(ConstantResponseKeys.STATUS, 200);
            } else if (isUpdated > 0) {
                responseObject.put(ConstantResponseKeys.MESSAGE, "The group has been updated successfully.");
                responseObject.put(ConstantResponseKeys.STATUS, 200);
            } else {
                responseObject.put(ConstantResponseKeys.MESSAGE, "No record found to update.");
                responseObject.put(ConstantResponseKeys.STATUS, 404);
            }
            // Evict user from redis
            deleteCacheUser(loggedUser.getSlug());
        } else { // Going to insert a new group
            logger.debug("We are going to create the group.");
            Group group = new Group(loggedUser);
            group.setName(groupRequest.getName());
            Group insertedGroup = groupRepository.save(group);
            // Updating given permissions.
            permissionHbRepository.updatePermissions(insertedGroup.getId(), groupRequest.getPermissions());
            responseObject.put(ConstantResponseKeys.RES, insertedGroup);
            responseObject.put(ConstantResponseKeys.MESSAGE, groupRequest.getName() + " successfully created.");
            responseObject.put(ConstantResponseKeys.STATUS, 201);
        }
        logger.debug("Exiting createOrUpdateGroup with responseObject: {}", responseObject);
        return responseObject;
    }

    public Map<String, Object> findGroupBySlug(String slug) {
        logger.debug("Entering findGroupBySlug with slug: {}", slug);
        if (Utils.isEmpty(slug)) throw new IllegalArgumentException("slug can't be null");
        Group group = groupRepository.findGroupBySlug(slug);
        if (group == null) throw new NotFoundException("No record found.");

        List<Map<String, Object>> groupWithPermission = groupRepository.findGroupAndPermissionsByGroupId(group.getId());

        Map<String, Object> formattedGroup = new HashMap<>();
        List<Integer> permissionList = new ArrayList<>();
        // Only getting permission id list
        for (Map<String, Object> map : groupWithPermission) {
            permissionList.add((Integer) map.get("id"));
        }
        formattedGroup.put("group", group.getName());
        formattedGroup.put("permissions", permissionList);

        logger.debug("Exiting findGroupBySlug with formattedGroup: {}", formattedGroup);
        return formattedGroup;
    }

    public Map<String, List<Object>> getAllPermissions() {
        logger.debug("Entering getAllPermissions");
        List<Permission> permissionList = permissionRepository.findAll();
        Map<String, List<Object>> formattedPermissions = new HashMap<>();
        for (Permission permission : permissionList) {
            String key = permission.getPermissionFor();
            if (formattedPermissions.containsKey(key)) {
                List<Object> addedPermissions = formattedPermissions.get(key);
                addedPermissions.add(permission);
                formattedPermissions.put(key, addedPermissions);
            } else {
                List<Object> newPermissions = new ArrayList<>();
                newPermissions.add(permission);
                formattedPermissions.put(key, newPermissions);
            }
        }
        logger.debug("Exiting getAllPermissions with formattedPermissions: {}", formattedPermissions);
        return formattedPermissions;
    }

    @Transactional(rollbackFor = {IllegalArgumentException.class, PermissionDeniedDataAccessException.class, RuntimeException.class, Exception.class})
    public int deleteGroupBySlug(DeleteRequest deleteRequest, AuthUser loggedUser) throws Exception {
        logger.debug("Entering deleteGroupBySlug with deleteRequest: {}, loggedUser: {}", deleteRequest, loggedUser);
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(deleteRequest, List.of("slug"));

        //Only super admin can create or update a group.
        if (!loggedUser.getUserType().equals("SA"))
            throw new PermissionDeniedDataAccessException("You don't have permission to delete a group. Please contact a super admin", null);

        String slug = deleteRequest.getSlug();
        Group group = groupRepository.findGroupBySlug(slug);
        if (group == null) throw new NotFoundException("No group found to delete");
        int result = permissionHbRepository.deleteGroupBySlug(slug, group.getId(),(loggedUser.getId() == GlobalConstant.suId));
        logger.debug("Exiting deleteGroupBySlug with result: {}", result);
        return result;
    }

    public int assignGroupsToUser(UserPermissionsRequest userPermissionsRequest, AuthUser loggedUser) throws Exception {
        logger.debug("Entering assignGroupsToUser with userPermissionsDto: {}, loggedUser: {}", userPermissionsRequest, loggedUser);
        int userId = userPermissionsRequest.getUserId();
        int result = permissionHbRepository.assignGroupsToUser(userId, userPermissionsRequest.getGroupList(), loggedUser);
        logger.debug("Exiting assignGroupsToUser with result: {}", result);
        return result;
    }

    private void deleteCacheUser(String slug){
        try {
            userCacheService.evictCacheUser(slug);
        }catch (Exception e){
            logger.warn("Facing issue when going to delete user from redis : {}",slug,e);
        }
    }
}
