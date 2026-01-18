package com.sales.admin.controllers;


import com.sales.admin.services.ServicePlanService;
import com.sales.admin.services.UserService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.DeleteDto;
import com.sales.dto.ServicePlanDto;
import com.sales.dto.StatusDto;
import com.sales.dto.UserPlanDto;
import com.sales.entities.ServicePlan;
import com.sales.entities.WholesalerPlans;
import com.sales.global.ConstantResponseKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("admin/plans/")
@RequiredArgsConstructor
@Tag(name = "Service Plans", description = "APIs for managing service plans and user plans")
public class ServicePlanController  {

    private final UserService userService;
    private final ServicePlanService servicePlanService;
    
    private static final Logger logger = LoggerFactory.getLogger(ServicePlanController.class);


    @PostMapping(value = {"user-plans/{userSlug}","user-plans"})
    @PreAuthorize("hasAnyAuthority('user.plan.all','user.plan.detail')")
    @Operation(summary = "Get user plans", description = "Retrieves a paginated list of plans for a specific user or all users")
    public ResponseEntity< Page<WholesalerPlans>> getUserPlans(@PathVariable(required = false) String userSlug, @RequestBody UserPlanDto searchFilters){
        logger.debug("Fetching user plans for userSlug: {}", userSlug);
        Integer userId = userService.getUserIdBySlug(userSlug);
        Page<WholesalerPlans> allUserPlans = servicePlanService.getAllUserPlans(userId, searchFilters);
        return new ResponseEntity<>(allUserPlans,HttpStatus.OK);
    }


    @PostMapping("service-plans")
    @PreAuthorize("hasAuthority('service-plans.all')")
    @Operation(summary = "Get all service plans", description = "Retrieves a paginated list of all service plans with optional filters")
    public ResponseEntity<Page<ServicePlan>> getAllPlans(@RequestBody ServicePlanDto servicePlanDto) {
        logger.debug("Fetching all service plans with filters: {}", servicePlanDto);
        return new ResponseEntity<>(servicePlanService.getALlServicePlan(servicePlanDto), HttpStatusCode.valueOf(200));
    }


    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(
            example = """
               {
                     "planName": "string",
                      "months": 0,
                      "price": 0,
                      "discount": 0,
                      "description": "string"
                }
            """))
    )
    @PostMapping("add")
    @PreAuthorize("hasAuthority('service-plans.add')")
    @Operation(summary = "Add service plan", description = "Creates a new service plan")
    public ResponseEntity<Map<String,Object>> insertServicePlans(Authentication authentication,HttpServletRequest request , @RequestBody ServicePlanDto servicePlanDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Inserting new service plan: {}", servicePlanDto);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Map<String,Object> result = new HashMap<>();
        ServicePlan servicePlan = servicePlanService.insertServicePlan(loggedUser,servicePlanDto);
        result.put(ConstantResponseKeys.RES,servicePlan);
        result.put(ConstantResponseKeys.MESSAGE,"Service plan added successfully.");
        result.put(ConstantResponseKeys.STATUS , 201);
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }


    @PreAuthorize("hasAuthority('service-plans.status.update')")
    @PostMapping(ConstantResponseKeys.STATUS)
    @Operation(summary = "Update service plan status", description = "Updates the status of a service plan")
    public ResponseEntity<Map<String,Object>> updateStatus(Authentication authentication,HttpServletRequest request, @RequestBody StatusDto statusDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Updating status for service plan: {}", statusDto);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Map<String, Object> result = servicePlanService.updateServicePlanStatus(statusDto, loggedUser);
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }


    @PreAuthorize("hasAuthority('service-plans.delete')")
    @PostMapping("delete")
    @Operation(summary = "Delete service plan", description = "Deletes a service plan")
    public ResponseEntity<Map<String,Object>> deleteStatus(Authentication authentication,@RequestBody DeleteDto deleteDto, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Deleting service plan: {}", deleteDto);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Map<String, Object> result = servicePlanService.deletedServicePlan(deleteDto,loggedUser);
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }

}
