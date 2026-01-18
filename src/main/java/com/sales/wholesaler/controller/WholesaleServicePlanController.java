package com.sales.wholesaler.controller;


import com.sales.cachemanager.services.UserCacheService;
import com.sales.claims.AuthUser;
import com.sales.dto.UserPlanDto;
import com.sales.entities.ServicePlan;
import com.sales.entities.WholesalerPlans;
import com.sales.global.ConstantResponseKeys;
import com.sales.jwtUtils.JwtToken;
import com.sales.utils.Utils;
import com.sales.wholesaler.services.WholesaleServicePlanService;
import com.sales.wholesaler.services.WholesaleUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("wholesale/plan")
@RequiredArgsConstructor
@Tag(name = "Wholesale Service Plan Management", description = "APIs for managing service plans for wholesalers")
public class WholesaleServicePlanController  {

    private final WholesaleServicePlanService wholesaleServicePlanService;
    private final JwtToken jwtToken;
    private final WholesaleUserService wholesaleUserService;
    private final UserCacheService userCacheService;
    private static final Logger logger = LoggerFactory.getLogger(WholesaleServicePlanController.class);

    @GetMapping("/all")
    @Operation(summary = "Get all service plans", description = "Retrieves a list of all available service plans")
    public ResponseEntity<List<ServicePlan>> getAllPlans() {
        logger.debug("Starting getAllPlans method");
        ResponseEntity<List<ServicePlan>> response = new ResponseEntity<>(wholesaleServicePlanService.getAllServicePlan(), HttpStatusCode.valueOf(200));
        logger.debug("Completed getAllPlans method");
        return response;
    }


    @GetMapping("detail/{slug}")
    @PreAuthorize("hasAuthority('wholesale.plan.detail')")
    @Operation(summary = "Get plan details by slug", description = "Retrieves detailed information for a specific service plan using its slug")
    public ResponseEntity<ServicePlan> getPlanDetailBySlug(@PathVariable String slug) {
        logger.debug("Starting getPlanDetailBySlug method");
        ResponseEntity<ServicePlan> response = new ResponseEntity<>(wholesaleServicePlanService.findBySlug(slug), HttpStatusCode.valueOf(200));
        logger.debug("Completed getPlanDetailBySlug method");
        return response;
    }

    @PostMapping("/my-plans")
    @Operation(summary = "Get my plans", description = "Retrieves a paginated list of all plans associated with the authenticated wholesaler")
    public ResponseEntity<Page<WholesalerPlans>> getMyAllPlans(HttpServletRequest request, @RequestBody UserPlanDto searchFilters) {
        logger.debug("Starting getMyAllPlans method");
        AuthUser loggedUser = Utils.getUserFromRequest(request, jwtToken, wholesaleUserService);
        Page<WholesalerPlans> allUserPlans = wholesaleServicePlanService.getAllUserPlans(loggedUser, searchFilters);
        logger.debug("Completed getMyAllPlans method");
        return new ResponseEntity<>(allUserPlans, HttpStatusCode.valueOf(200));
    }

    @GetMapping("is-active")
//    @PreAuthorize("hasAuthority('wholesale.plan.active')")
    @Operation(summary = "Check if user plan is active", description = "Checks whether the authenticated wholesaler's current plan is active")
    public ResponseEntity<Map<String,Object>> isUserPlanActive(HttpServletRequest request){
        logger.debug("Starting isUserPlanActive method");
        AuthUser loggedUser = Utils.getUserFromRequest(request,jwtToken,wholesaleUserService);
        Map<String,Object> result = new HashMap<>();
        boolean planIsActive = wholesaleServicePlanService.isPlanActive(loggedUser.getActivePlan());
        result.put("planIsActive",planIsActive);
        result.put("status" , 200);
        logger.debug("Completed isUserPlanActive method");
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get("status")));
    }

    @GetMapping("activate/{planSlug}")
//    @PreAuthorize("hasAuthority('wholesale.my.current.plan')")
    @Operation(summary = "Activate user plan", description = "Activates a specific service plan for the authenticated wholesaler")
    public ResponseEntity<Map<String,Object>> updateMyCurrentPlan(HttpServletRequest request , @PathVariable String planSlug){
        AuthUser loggedUser = Utils.getUserFromRequest(request,jwtToken,wholesaleUserService);
        Map<String,Object> result = new HashMap<>();
        int isUpdated = wholesaleServicePlanService.updatedUserCurrentPlan(planSlug,loggedUser);
        if(isUpdated > 0){
            result.put(ConstantResponseKeys.MESSAGE,"Your current plan activated successfully");
            result.put(ConstantResponseKeys.STATUS,200);
        }else {
            result.put(ConstantResponseKeys.MESSAGE,"No record found to update.");
            result.put(ConstantResponseKeys.STATUS,404);
        }
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get("status")));
    }

}
