package com.sales.wholesaler.services;


import com.sales.claims.AuthUser;
import com.sales.request.UserPlanRequest;
import com.sales.entities.ServicePlan;
import com.sales.entities.WholesalerFuturePlan;
import com.sales.entities.WholesalerPlans;
import com.sales.exceptions.NotFoundException;
import com.sales.utils.Utils;
import com.sales.wholesaler.dto.WholesalerPlanDto;
import com.sales.wholesaler.dto.WholesaleServicePlanDto;
import com.sales.wholesaler.mapper.WholesaleServicePlanMapper;
import com.sales.wholesaler.mapper.WholesalerPlanMapper;
import com.sales.wholesaler.repository.WholesaleFuturePlansRepository;
import com.sales.wholesaler.repository.WholesaleServicePlanRepository;
import com.sales.wholesaler.repository.WholesaleUserHbRepository;
import com.sales.wholesaler.repository.WholesaleUserPlansRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.sales.helpers.PaginationHelper.getPageable;
import static com.sales.specifications.PlansSpecifications.*;

@Service
@RequiredArgsConstructor
public class WholesaleServicePlanService {

    private final EntityManager entityManager;
    private final WholesaleServicePlanRepository wholesaleServicePlanRepository;
    private final WholesaleUserPlansRepository wholesaleUserPlansRepository;
    private final WholesaleUserHbRepository wholesaleUserHbRepository;
    private final WholesaleFuturePlansRepository wholesaleFuturePlansRepository;
    private final WholesaleServicePlanMapper wholesaleServicePlanMapper;
    private final WholesalerPlanMapper wholesalerPlanMapper;

    private static final Logger logger = LoggerFactory.getLogger(WholesaleServicePlanService.class);

    @Transactional
    public List<WholesaleServicePlanDto> getAllServicePlan() {
        logger.debug("Starting getALlServicePlan method");
        List<WholesaleServicePlanDto> servicePlans = wholesaleServicePlanRepository.findAll().stream().filter(servicePlan -> servicePlan.getPrice() > 0).map(wholesaleServicePlanMapper::toDto).toList();
        logger.debug("Completed getALlServicePlan method");
        return servicePlans;
    }

    @Transactional
    public WholesaleServicePlanDto findBySlug(String slug) {
        logger.debug("Starting findBySlug method with params: {}", slug);
        ServicePlan servicePlan = wholesaleServicePlanRepository.findBySlug(slug);
        logger.debug("Completed findBySlug method");
        return wholesaleServicePlanMapper.toDto(servicePlan);
    }

    public boolean isPlanActive(Integer userPlanId) {
        logger.debug("Starting isPlanActive method with userPlanId: {}", userPlanId);
        if (userPlanId == null) return false;
        Optional<WholesalerPlans> plan = wholesaleUserPlansRepository.findById(userPlanId);
        if (plan.isPresent()) {
            WholesalerPlans userPlan = plan.get();
            long expiryDate = userPlan.getExpiryDate();
            long currentDate = Utils.getCurrentMillis();
            boolean isActive = currentDate <= expiryDate;
            logger.debug("Completed isPlanActive method");
            return isActive;
        }
        logger.debug("Completed isPlanActive method");
        return false;
    }

    public void assignOrAddFuturePlans(int userId, int servicePlanId) {
        logger.debug("Starting assignOrAddFuturePlans method with userId: {}, servicePlanId: {}", userId, servicePlanId);
        Long currentMillis = Utils.getCurrentMillis();
        // Checking user last plan expired or not.
        WholesalerPlans lastPlan = wholesaleUserPlansRepository.findLastPlanByUserId(userId, entityManager);
        ServicePlan plan = wholesaleServicePlanRepository.findById(servicePlanId).orElseThrow(() -> new NotFoundException("Plan not found."));

        if (lastPlan != null && lastPlan.getExpiryDate() > currentMillis) { // if last plans is not expired.
            logger.debug("Going to adding this plan as future plan.");
            WholesalerFuturePlan wholesalerFuturePlan = WholesalerFuturePlan.builder()
                    .userId(userId)
                    .slug(UUID.randomUUID().toString())
                    .servicePlan(plan)
                    .status("N") // It's a new future plan.
                    .createdAt(Utils.getCurrentMillis())
                    .build();
            wholesaleFuturePlansRepository.save(wholesalerFuturePlan);
        } else { // Going to assign plan directly to user.
            assignUserPlan(userId, plan);
        }
        logger.debug("Completed assignOrAddFuturePlans method");
    }


    public void assignUserPlan(int userId, ServicePlan plan) {
        logger.debug("Starting assignUserPlan(int userId, ServicePlan plan) method with userId: {}, servicePlanId: {}", userId, plan.getId());
        Long currentMillis = Utils.getCurrentMillis();
        logger.debug("Going to assign this plan as user current plan.");
        Integer months = plan.getMonths();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentMillis);
        calendar.add(Calendar.MONTH, months);
        long expiryDate = calendar.getTimeInMillis();
        WholesalerPlans userPlans = WholesalerPlans.builder()
                .slug(UUID.randomUUID().toString())
                .userId(userId)
                .servicePlan(plan)
                .createdAt(currentMillis)
                .expiryDate(expiryDate)
                .createdBy(userId)
                .build();
        WholesalerPlans userPlan = wholesaleUserPlansRepository.save(userPlans); // Create operation
        int updated = wholesaleUserHbRepository.updateUserActivePlan(userId, userPlan.getId());// Update operation
        if (updated < 1) {
            throw new NotFoundException("No user found. to assign this plan.");
        }
        logger.debug("Completed assignUserPlan method");
    }


    // This method is overloaded.
    public void assignUserPlan(int userId, int servicePlanId) {
        logger.debug("Starting assignUserPlan(int userId, int servicePlanId) method with userId: {}, servicePlanId: {}", userId, servicePlanId);
        Long currentMillis = Utils.getCurrentMillis();
        ServicePlan plan = wholesaleServicePlanRepository.findById(servicePlanId).orElseThrow(() -> new NotFoundException("Service plan not found."));
        logger.debug("Going to assign this plan as user current plan : {}.", servicePlanId);
        Integer months = plan.getMonths();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentMillis);
        calendar.add(Calendar.MONTH, months);
        long expiryDate = calendar.getTimeInMillis();
        WholesalerPlans userPlans = WholesalerPlans.builder()
                .slug(UUID.randomUUID().toString())
                .userId(userId)
                .servicePlan(plan)
                .createdAt(currentMillis)
                .expiryDate(expiryDate)
                .createdBy(userId)
                .build();
        WholesalerPlans userPlan = wholesaleUserPlansRepository.save(userPlans); // Create operation
        int updated = wholesaleUserHbRepository.updateUserActivePlan(userId, userPlan.getId());// Update operation
        if (updated < 1) {
            throw new NotFoundException("No user found. to assign this plan.");
        }
        logger.debug("Completed assignUserPlan method.");
    }


    @Transactional
    public Page<WholesalerPlanDto> getAllUserPlans(AuthUser loggedUser, UserPlanRequest searchFilters) {
        logger.debug("Starting getAllUserPlans method with loggedUser: {}, searchFilters: {}", loggedUser, searchFilters);
        Specification<WholesalerPlans> specification = Specification.allOf(
                hasSlug(searchFilters.getSlug())
                        .and(greaterThanOrEqualCreatedFromDate(searchFilters.getCreatedFromDate()))
                        .and(lessThanOrEqualToCreatedToDate(searchFilters.getCreatedToDate()))
                        .and(greaterThanOrEqualExpiredFromDate(searchFilters.getExpiredFromDate()))
                        .and(lessThanOrEqualToExpiredToDate(searchFilters.getExpiredToDate()))
                        .and(isStatus(searchFilters.getStatus()))
                        .and(isUserId(loggedUser.getId()))
        );
        Pageable pageable = getPageable(logger, searchFilters);
        Page<WholesalerPlans> userPlans = wholesaleUserPlansRepository.findAll(specification, pageable);
        Page<WholesalerPlanDto> wholesalerPlans = userPlans.map(wholesalerPlan -> {
            if (Objects.nonNull(wholesalerPlan)) {
                wholesalerPlan.setExpired(wholesalerPlan.getExpiryDate() < Utils.getCurrentMillis());
            }
            return wholesalerPlanMapper.toDto(wholesalerPlan);
        });
        logger.debug("Completed getAllUserPlans method");
        return wholesalerPlans;
    }

    public ServicePlan getDefaultServicePlan() {
        logger.debug("Starting getDefaultServicePlan method");
        ServicePlan defaultServicePlan = wholesaleServicePlanRepository.getDefaultServicePlan();
        logger.debug("Completed getDefaultServicePlan method");
        return defaultServicePlan;
    }


    public int updatedUserCurrentPlan(String plansSlug, AuthUser loggedUser) {
        logger.debug("Starting updatedUserCurrentPlan method.");
        Integer wholesaleUserPlanId = wholesaleUserPlansRepository.getWholesaleUserPlanId(loggedUser.getId(), plansSlug);
        if (wholesaleUserPlanId == null) throw new IllegalArgumentException("Not a valid active plan.");
        int isUpdated = wholesaleUserHbRepository.updateUserActivePlan(loggedUser.getId(), wholesaleUserPlanId);
        logger.debug("Completed updatedUserCurrentPlan method with isUpdated  : {}.", isUpdated);
        return isUpdated;
    }


}
