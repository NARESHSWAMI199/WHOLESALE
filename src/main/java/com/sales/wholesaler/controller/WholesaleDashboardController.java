package com.sales.wholesaler.controller;


import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.GraphDto;
import com.sales.global.ConstantResponseKeys;
import com.sales.wholesaler.services.WholesaleItemService;
import com.sales.wholesaler.services.WholesaleStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("wholesale/dashboard")
@RequiredArgsConstructor
@Tag(name = "Wholesale Dashboard", description = "APIs for wholesale dashboard data")
public class WholesaleDashboardController  {


    private final WholesaleStoreService wholesaleStoreService;
    private final WholesaleItemService wholesaleItemService;
    private static final Logger logger = LoggerFactory.getLogger(WholesaleDashboardController.class);

    @GetMapping("/counts")
    @PreAuthorize("hasAuthority('wholesale.dashboard.count')")
    @Operation(summary = "Get dashboard counts", description = "Retrieves counts for items, new items, old items, in stock, and out of stock for the wholesale store")
    public ResponseEntity<Map<String, Object>> getAllDashboardCount(Authentication authentication,HttpServletRequest request) {
        logger.debug("Starting getAllDashboardCount method");
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Integer storeId = wholesaleStoreService.getStoreIdByUserSlug(loggedUser.getId());
        Map<String,Object> responseObj = new HashMap<>();
        responseObj.put("items" , wholesaleItemService.getItemCounts(storeId));
        responseObj.put("newItems" , wholesaleItemService.getItemCountsForNewLabel(storeId));
        responseObj.put("oldItems" , wholesaleItemService.getItemCountsForOldLabel(storeId) );
        responseObj.put("inStock" , wholesaleItemService.getItemCountsForInStock(storeId));
        responseObj.put("outStock" , wholesaleItemService.getItemCountsForOutStock(storeId));
        responseObj.put(ConstantResponseKeys.STATUS, 200);
        logger.debug("Completed getAllDashboardCount method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

    @PostMapping("graph/months/")
    @PreAuthorize("hasAuthority('wallet.dashboard.graph')")
    @Operation(summary = "Get graph data by months", description = "Retrieves graph data for item counts by months based on filters for the wholesale store")
    public ResponseEntity<Map<String, Object>> getAllGraphData(Authentication authentication, HttpServletRequest request, @RequestBody GraphDto graphDto) {
        logger.debug("Starting getAllGraphData method");
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Integer storeId = wholesaleStoreService.getStoreIdByUserSlug(loggedUser.getId());
        responseObj.put(ConstantResponseKeys.RES ,wholesaleItemService.getItemCountByMonths(graphDto,storeId));
        responseObj.put(ConstantResponseKeys.STATUS, 200);
        logger.debug("Completed getAllGraphData method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }
}
