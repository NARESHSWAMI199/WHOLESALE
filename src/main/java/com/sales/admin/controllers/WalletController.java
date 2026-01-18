package com.sales.admin.controllers;


import com.sales.admin.services.WalletService;
import com.sales.entities.Wallet;
import com.sales.global.ConstantResponseKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("admin/store/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet Management", description = "APIs for managing user wallets and payments")
public class WalletController  {

    private final WalletService walletService;

    @PreAuthorize("hasAuthority('wallet.detail')")
    @GetMapping("/{userSlug}")
    @Operation(summary = "Get wallet details", description = "Retrieves wallet details for a specific user")
    public ResponseEntity<Wallet> getWalletDetail(@PathVariable String userSlug, HttpServletRequest request){
        Wallet walletDetail = walletService.getWalletDetail(userSlug);
        return new ResponseEntity<>(walletDetail, HttpStatus.OK);
    }


    @PreAuthorize("hasAuthority('wallet.pay')")
    @GetMapping("pay/{userSlug}/{servicePlanSlug}")
    @Operation(summary = "Pay using wallet", description = "Processes payment for a service plan using the user's wallet")
    public ResponseEntity<Map<String,Object>> payUsingWallet(@PathVariable String userSlug , @PathVariable String servicePlanSlug,HttpServletRequest request) {
        Map<String,Object> result = new HashMap<>();
        boolean payment = walletService.paymentViaWallet(servicePlanSlug, userSlug);
        if(payment){
            result.put(ConstantResponseKeys.MESSAGE,"Plan purchased successfully.");
            result.put(ConstantResponseKeys.STATUS,200);
        }else{
            result.put(ConstantResponseKeys.MESSAGE,"Inefficient amount in wallet.");
            result.put(ConstantResponseKeys.STATUS,400);
        }
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get("status")));
    }

}
