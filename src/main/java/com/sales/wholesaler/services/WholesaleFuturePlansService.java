package com.sales.wholesaler.services;


import com.sales.claims.AuthUser;
import com.sales.global.ResponseMessages;
import com.sales.request.SearchFilters;
import com.sales.entities.WholesalerFuturePlan;
import com.sales.exceptions.NotFoundException;
import com.sales.utils.Utils;
import com.sales.wholesaler.dto.WholesalerFuturePlanDto;
import com.sales.wholesaler.mapper.WholesalerFuturePlanMapper;
import com.sales.wholesaler.repository.WholesaleFuturePlansRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.sales.helpers.PaginationHelper.getPageable;

@Service
@RequiredArgsConstructor
public class WholesaleFuturePlansService  {

    private static final Logger logger = LoggerFactory.getLogger(WholesaleFuturePlansService.class);
    private final  WholesaleServicePlanService  wholesaleServicePlanService;
    private final WholesaleFuturePlansRepository wholesaleFuturePlansRepository;
    private final WholesalerFuturePlanMapper wholesalerFuturePlanMapper;

    @Transactional
    public Page<WholesalerFuturePlanDto> getWholesalerFuturePlans(AuthUser loggedUser, SearchFilters filters) {
        Pageable pageable = getPageable(logger,filters);
        Page<WholesalerFuturePlan> wholesalerFuturePlanPage = wholesaleFuturePlansRepository.findWholesalerFuturePlansByUserIdAndStatus(pageable, loggedUser.getId(), "N");// Getting only new not old or used.
        return wholesalerFuturePlanPage.map(wholesalerFuturePlanMapper::toDto);
    }



    public int activateWholesalerFuturePlans(AuthUser loggedUser,String futurePlanSlug){
        Map<String,Object> futurePlan = wholesaleFuturePlansRepository.getNewFuturePlanByUserIdAndSlug(futurePlanSlug,loggedUser.getId());
        Object servicePlanId = futurePlan.get("servicePlanId");
        Object wholesalerFuturePlanId = futurePlan.get("wholesalerFuturePlanId");
        if (servicePlanId == null || wholesalerFuturePlanId == null) throw new NotFoundException(ResponseMessages.NOT_A_VALID_REQUEST_FUTURE_PLAN_NOT_FOUND);
        wholesaleServicePlanService.assignUserPlan(loggedUser.getId(), (Integer) servicePlanId);
        return wholesaleFuturePlansRepository.updateWholesalerFuturePlans((Long) wholesalerFuturePlanId, Utils.getCurrentMillis());
    }


}
