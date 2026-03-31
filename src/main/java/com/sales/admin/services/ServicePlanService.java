package com.sales.admin.services;


import com.sales.admin.dto.PlanDto;
import com.sales.admin.dto.ServicePlanDto;
import com.sales.admin.mapper.PlanMapper;
import com.sales.admin.mapper.ServicePlanMapper;
import com.sales.admin.repositories.ServicePlanHbRepository;
import com.sales.admin.repositories.ServicePlanRepository;
import com.sales.admin.repositories.WholesalerPlansRepository;
import com.sales.claims.AuthUser;
import com.sales.entities.ServicePlan;
import com.sales.entities.WholesalerPlans;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.ResponseMessages;
import com.sales.global.USER_TYPES;
import com.sales.request.*;
import com.sales.specifications.PlansSpecifications;
import com.sales.specifications.ServicePlanSpecification;
import com.sales.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.sales.helpers.PaginationHelper.getPageable;

@Service
@RequiredArgsConstructor
public class ServicePlanService {

    private static final Logger logger = LoggerFactory.getLogger(ServicePlanService.class);
    private final ServicePlanRepository servicePlanRepository;
    private final WholesalerPlansRepository wholesalerPlansRepository;
    private final ServicePlanHbRepository servicePlanHbRepository;
    private final PlanMapper planMapper;
    private final ServicePlanMapper servicePlanMapper;

    @Transactional(readOnly = true)
    public Page<ServicePlanDto> getALlServicePlan(ServicePlanFilterRequest servicePlanFilterRequest) {
        logger.debug("Entering getALlServicePlan with servicePlanDto: {}", servicePlanFilterRequest);
        Specification<ServicePlan> specification = Specification.allOf(
                ServicePlanSpecification.containsName(servicePlanFilterRequest.getName())
                        .and(ServicePlanSpecification.hasSlug(servicePlanFilterRequest.getSlug()))
                        .and(ServicePlanSpecification.isStatus(servicePlanFilterRequest.getStatus()))
                        .and(ServicePlanSpecification.greaterThanOrEqualFromDate(servicePlanFilterRequest.getFromDate()))
                        .and(ServicePlanSpecification.lessThanOrEqualToToDate(servicePlanFilterRequest.getToDate()))
        );
        Pageable pageable = getPageable(logger, servicePlanFilterRequest);
        Page<ServicePlan> result = servicePlanRepository.findAll(specification, pageable);
        logger.debug("Exiting getALlServicePlan");
        return result.map(servicePlanMapper::toDto);
    }

    public ServicePlan findBySlug(String slug) {
        logger.debug("Entering findBySlug with slug: {}", slug);
        ServicePlan result = servicePlanRepository.findBySlug(slug);
        logger.debug("Exiting findBySlug");
        return result;
    }

    public boolean isPlanActive(Integer userPlanId) {
        logger.debug("Entering isPlanActive with userPlanId: {}", userPlanId);
        if (userPlanId == null) return false;
        Optional<WholesalerPlans> plan = wholesalerPlansRepository.findById(userPlanId);
        if (plan.isPresent()) {
            WholesalerPlans userPlan = plan.get();
            long expiryDate = userPlan.getExpiryDate();
            long currentDate = Utils.getCurrentMillis();
            boolean isActive = currentDate <= expiryDate;
            logger.debug("Exiting isPlanActive with result: {}", isActive);
            return isActive;
        }
        logger.debug("Exiting isPlanActive with result: false");
        return false;
    }

    @Transactional
    public Page<PlanDto> getAllUserPlans(Integer userId, UserPlanRequest searchFilters) {
        logger.debug("Entering getAllUserPlans with userId: {}, searchFilters: {}", userId, searchFilters);
        Specification<WholesalerPlans> specification = Specification.allOf(
                PlansSpecifications.hasSlug(searchFilters.getSlug())
                        .and(PlansSpecifications.greaterThanOrEqualCreatedFromDate(searchFilters.getCreatedFromDate()))
                        .and(PlansSpecifications.lessThanOrEqualToCreatedToDate(searchFilters.getCreatedToDate()))
                        .and(PlansSpecifications.greaterThanOrEqualExpiredFromDate(searchFilters.getExpiredFromDate()))
                        .and(PlansSpecifications.lessThanOrEqualToExpiredToDate(searchFilters.getExpiredToDate()))
                        .and(PlansSpecifications.isStatus(searchFilters.getStatus()))
                        .and(PlansSpecifications.isUserId(userId))
        );
        Pageable pageable = getPageable(logger, searchFilters);
        Page<WholesalerPlans> result = wholesalerPlansRepository.findAll(specification, pageable);
        logger.debug("Exiting getAllUserPlans");
        return result.map(planMapper::toDto);
    }


    @Transactional
    public ServicePlan insertServicePlan(AuthUser loggedUser, ServicePlanCreateRequest servicePlanRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering insertServicePlan with loggedUser: {}, servicePlanDto: {}", loggedUser, servicePlanRequest);
        if (!loggedUser.getUserType().equals("SA"))
            throw new PermissionDeniedDataAccessException(ResponseMessages.PERMISSION_DENIED_ACTION_CONTACT_ADMINISTRATOR, new Exception());

        // Validating required fields if there we found any required field is null, then it will throw an Exception
        Utils.checkRequiredFields(servicePlanRequest, List.of("planName", "price", "discount", "months", "description"));

        if (servicePlanRequest.getPrice() < 0)
            throw new IllegalArgumentException(ResponseMessages.PRICE_CAN_T_BE_LESS_THAN_0);
        if (servicePlanRequest.getDiscount() < 0 || servicePlanRequest.getDiscount() > servicePlanRequest.getPrice())
            throw new IllegalArgumentException(ResponseMessages.DISCOUNT_CAN_T_BE_GREATER_THAN_PRICE_AND_CAN_T_BE_LESS_THAN_0);

        ServicePlan servicePlan = ServicePlan.builder()
                .name(servicePlanRequest.getPlanName())
                .price(servicePlanRequest.getPrice())
                .discount(servicePlanRequest.getDiscount())
                .months(servicePlanRequest.getMonths())
                .description(servicePlanRequest.getDescription())
                .createdBy(loggedUser.getId())
                .updatedBy(loggedUser.getId())
                .slug(UUID.randomUUID().toString())
                .status("A")
                .createdAt(Utils.getCurrentMillis())
                .updatedAt(Utils.getCurrentMillis())
                .isDeleted("N")
                .build();
        ServicePlan result = servicePlanRepository.save(servicePlan);
        logger.debug("Exiting insertServicePlan");
        return result;
    }

    public Map<String, Object> updateServicePlanStatus(StatusRequest statusRequest, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering updateServicePlanStatus with statusRequest: {}, loggedUser: {}", statusRequest, loggedUser);
        // Validating required fields if there we found any required field is null, then it will throw an Exception
        Utils.checkRequiredFields(statusRequest, List.of("status", "slug"));

        String status = statusRequest.getStatus();
        Map<String, Object> result = new HashMap<>();
        if (!loggedUser.getUserType().equals(USER_TYPES.SUPER_ADMIN.getType()))
            throw new PermissionDeniedDataAccessException(ResponseMessages.PERMISSION_DENIED_ACTION_CONTACT_ADMINISTRATOR, new Exception());

        switch (status) {
            case "A", "D":
                int isUpdated = servicePlanHbRepository.updateServicePlansStatus(status, statusRequest.getSlug(), loggedUser);
                if (isUpdated > 0) {
                    if (status.equals("A")) {
                        result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SUCCESSFULLY_ACTIVATED);
                    } else {
                        result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SUCCESSFULLY_DEACTIVATED);
                    }
                    result.put(ConstantResponseKeys.STATUS, 200);
                } else {
                    result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.NO_PLAN_FOUND_TO_UPDATE);
                    result.put(ConstantResponseKeys.STATUS, 404);
                }
                logger.debug("Exiting updateServicePlanStatus with result: {}", result);
                return result;
            default:
                throw new IllegalArgumentException(ResponseMessages.STATUS_MUST_BE_A_OR_D_1);
        }
    }

    public Map<String, Object> deletedServicePlan(DeleteRequest deleteRequest, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering deletedServicePlan with deleteRequest: {}, loggedUser: {}", deleteRequest, loggedUser);
        // Validating required fields if their we found any required field is null, then it will throw an Exception
        Utils.checkRequiredFields(deleteRequest, List.of("slug"));
        String slug = deleteRequest.getSlug();
        Map<String, Object> result = new HashMap<>();
        if (!loggedUser.getUserType().equals(USER_TYPES.SUPER_ADMIN.getType()))
            throw new PermissionDeniedDataAccessException(ResponseMessages.PERMISSION_DENIED_ACTION_CONTACT_ADMINISTRATOR, new Exception());
        int isUpdated = servicePlanHbRepository.deleteServicePlan(slug, loggedUser);
        if (isUpdated > 0) {
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SERVICE_PLAN_SUCCESSFULLY_DELETED);
            result.put(ConstantResponseKeys.STATUS, 200);
        } else {
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.NO_SERVICE_PLAN_FOUND_TO_DELETE);
            result.put(ConstantResponseKeys.STATUS, 404);
        }
        logger.debug("Exiting deletedServicePlan with result: {}", result);
        return result;
    }


}
