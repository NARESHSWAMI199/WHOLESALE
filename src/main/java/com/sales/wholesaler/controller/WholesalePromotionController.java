package com.sales.wholesaler.controller;


import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.StorePromotionDto;
import com.sales.wholesaler.services.WholesalePromotionsService;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("wholesale/promotions")
@RequiredArgsConstructor
@Tag(name = "Wholesale Promotion Management", description = "APIs for managing item promotions for wholesalers")
public class WholesalePromotionController  {

    private final WholesalePromotionsService wholesalePromotionsService;
    private static final Logger logger = LoggerFactory.getLogger(WholesalePromotionController.class);

    @PostMapping("/")
    @PreAuthorize("hasAuthority('wholesale.promoted.item.add')")
    @Operation(summary = "Insert promoted item", description = "Adds a new promoted item for the wholesaler store")
    public ResponseEntity<Map<String,Object>> insertPromotedItem(Authentication authentication, HttpServletRequest request, @ModelAttribute StorePromotionDto storePromotionDto){
        logger.debug("Starting insertPromotedItem method");
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Map<String,Object> response = wholesalePromotionsService.insertItemPromotion(storePromotionDto,loggedUser);
        logger.debug("Completed insertPromotedItem method");
        return new ResponseEntity<>(response,HttpStatus.valueOf((Integer) response.get("status")));
    }


}
