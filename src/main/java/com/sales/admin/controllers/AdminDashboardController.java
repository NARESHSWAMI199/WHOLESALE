package com.sales.admin.controllers;

import com.sales.admin.services.StoreService;
import com.sales.admin.services.UserService;
import com.sales.claims.AuthUser;
import com.sales.global.ConstantResponseKeys;
import com.sales.request.GraphRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "APIs for admin dashboard data")
public class AdminDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    private final UserService userService;
    private final StoreService storeService;

    @GetMapping("/counts")
    @PreAuthorize("hasAuthority('dashboard.count')")
    @Operation(summary = "Get dashboard counts", description = "Retrieves counts for users, retailers, wholesalers, staffs, and admins")
    public ResponseEntity<Map<String, Object>> getAllDashboardCount(Authentication authentication) {
        logger.debug("Fetching all dashboard counts");
        Map<String, Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (AuthUser) authentication.getPrincipal();
        responseObj.put("users", userService.getUserCounts(loggedUser));
        responseObj.put("retailers", userService.getRetailersCounts());
        responseObj.put("wholesalers", userService.getWholesalersCounts());
        responseObj.put("staffs", userService.getStaffsCounts());
        responseObj.put("admins", userService.getAdminsCounts());
        // responseObj.put("items", itemService.getItemCounts());
        // responseObj.put("wholesales", storeService.getWholesaleCounts());
        return new ResponseEntity<>(responseObj, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('dashboard.count')")
    @PostMapping("graph/months/")
    @Operation(summary = "Get graph data by months", description = "Retrieves graph data for store counts by months based on filters")
    public ResponseEntity<Map<String, Object>> getAllGraphData(@RequestBody GraphRequest graphRequest) {
        logger.debug("Fetching graph data for months with filters: {}", graphRequest);
        Map<String, Object> responseObj = new HashMap<>();
        responseObj.put(ConstantResponseKeys.RES, storeService.getStoreCountByMonths(graphRequest));
        responseObj.put(ConstantResponseKeys.STATUS, 200);
        return new ResponseEntity<>(responseObj, HttpStatus.OK);
    }
}
