package com.sales.admin.controllers;


import com.sales.admin.services.GroupService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.DeleteDto;
import com.sales.dto.GroupDto;
import com.sales.dto.SearchFilters;
import com.sales.entities.Group;
import com.sales.global.ConstantResponseKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("group")
@RequiredArgsConstructor
@Tag(name = "Group Management", description = "APIs for managing user groups and permissions")
public class GroupController  {

    private final GroupService groupService;
    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    @PostMapping("/all")
    @PreAuthorize("hasAuthority('group.all')")
    @Operation(summary = "Get all groups", description = "Retrieves a paginated list of all groups with optional search filters")
    public ResponseEntity<Page<Group>> getAllGroup(Authentication authentication,HttpServletRequest request, @RequestBody SearchFilters searchFilters) {
        logger.debug("Fetching all groups with filters: {}", searchFilters);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Page<Group> storePage = groupService.getAllGroups(searchFilters, loggedUser);
        return new ResponseEntity<>(storePage, HttpStatus.OK);
    }

    @GetMapping("/permissions/all")
    @PreAuthorize("hasAuthority('group.permission.all')")
    @Operation(summary = "Get all permissions", description = "Retrieves all available permissions grouped by category")
    public ResponseEntity<Map<String, List<Object>>> getAllPermissions(HttpServletRequest request) {
        logger.debug("Fetching all permissions");
        Map<String, List<Object>> permissions = groupService.getAllPermissions();
        return new ResponseEntity<>(permissions, HttpStatus.OK);
    }

    @Transactional
    @PostMapping(value = {"create", "update"})
    @PreAuthorize("hasAnyAuthority('group.permission.add','group.permission.update','group.permission.edit')")
    @Operation(summary = "Create or update group", description = "Creates a new group or updates an existing group based on the provided data")
    public ResponseEntity<Map<String, Object>> createOrUpdate(Authentication authentication,HttpServletRequest request, @RequestBody GroupDto groupDto) throws Exception {
        logger.debug("Creating or updating group: {}", groupDto);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        String path = request.getRequestURI();
        Map<String, Object> response = groupService.createOrUpdateGroup(groupDto, loggedUser, path);
        return new ResponseEntity<>(response, HttpStatus.valueOf((Integer) response.get(ConstantResponseKeys.STATUS)));
    }

    @GetMapping("/detail/{slug}")
    @PreAuthorize("hasAuthority('group.detail')")
    @Operation(summary = "Get group details", description = "Retrieves detailed information about a specific group by its slug")
    public ResponseEntity<Map<String, Object>> getDetailGroup(@PathVariable String slug) {
        logger.debug("Fetching group details for slug: {}", slug);
        Map<String, Object> responseObj = new HashMap<>();
        Map<String, Object> group = groupService.findGroupBySlug(slug);
        responseObj.put(ConstantResponseKeys.RES, group);
        responseObj.put(ConstantResponseKeys.STATUS, 200);
        return new ResponseEntity<>(responseObj, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('group.delete')")
    @Operation(summary = "Delete group", description = "Deletes a group by its slug")
    public ResponseEntity<Map<String, Object>> deleteGroupBySlug(Authentication authentication,HttpServletRequest request, @RequestBody DeleteDto deleteDto) throws Exception {
        logger.debug("Deleting group with slug: {}", deleteDto);
        Map<String, Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isUpdated = groupService.deleteGroupBySlug(deleteDto, loggedUser);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "User has been successfully deleted.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No group found to delete");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

}
