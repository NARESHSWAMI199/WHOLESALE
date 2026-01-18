package com.sales.wholesaler.controller;


import com.sales.claims.AuthUser;
import com.sales.entities.Wallet;
import com.sales.global.ConstantResponseKeys;
import com.sales.jwtUtils.JwtToken;
import com.sales.utils.Utils;
import com.sales.wholesaler.services.WholesaleUserService;
import com.sales.wholesaler.services.WholesaleWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("wholesale/wallet")
@RequiredArgsConstructor
@Tag(name = "Wholesale Wallet Management", description = "APIs for managing wallet operations for wholesalers")
public class WholesaleWalletController  {

    private final JwtToken jwtToken;
    private final WholesaleUserService wholesaleUserService;
    private final WholesaleWalletService wholesaleWalletService;

    @GetMapping("/")
    @Operation(summary = "Get wallet details", description = "Retrieves the wallet details for the authenticated wholesaler user")
    public ResponseEntity<Wallet> getWalletDetail(HttpServletRequest request){
        AuthUser loggedUser = Utils.getUserFromRequest(request,jwtToken,wholesaleUserService);
        Wallet walletDetail = wholesaleWalletService.getWalletDetail(loggedUser.getId());
        return new ResponseEntity<>(walletDetail, HttpStatus.OK);
    }


    @GetMapping("pay/{servicePlanSlug}")
//    @PreAuthorize("hasAuthority('wholesale.wallet.pay')")
    @Operation(summary = "Pay using wallet", description = "Makes a payment for a service plan using the wholesaler's wallet balance")
    public ResponseEntity<Map<String,Object>> payUsingWallet(@PathVariable String servicePlanSlug,HttpServletRequest request) {
        AuthUser loggedUser = Utils.getUserFromRequest(request,jwtToken,wholesaleUserService);
        Map<String,Object> result = new HashMap<>();
        boolean payment = wholesaleWalletService.paymentViaWallet(servicePlanSlug, loggedUser);
        if(payment){
            result.put(ConstantResponseKeys.MESSAGE,"Plan purchased successfully.");
            result.put(ConstantResponseKeys.STATUS,200);
        }else{
            result.put(ConstantResponseKeys.MESSAGE,"Inefficient amount in wallet.");
            result.put("status",400);
        }
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get("status")));
    }

}
