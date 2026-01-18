package com.sales.wholesaler.controller;


import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.SearchFilters;
import com.sales.entities.WalletTransaction;
import com.sales.jwtUtils.JwtToken;
import com.sales.wholesaler.services.WalletTransactionService;
import com.sales.wholesaler.services.WholesaleUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("wholesale/wallet/transactions/")
@RequiredArgsConstructor
@Tag(name = "Wholesale Wallet Transaction Management", description = "APIs for managing wallet transactions for wholesalers")
public class WalletTransactionController  {


    private final JwtToken jwtToken;
    private final WholesaleUserService wholesaleUserService;
    private final WalletTransactionService walletTransactionService;

    @PostMapping("all")
    @PreAuthorize("hasAuthority('wallet.transactiona.all')")
    @Operation(summary = "Get all wallet transactions", description = "Retrieves a paginated list of all wallet transactions for the authenticated wholesaler")
    public ResponseEntity<Page<WalletTransaction>> getAllWalletTransactionsByUserId(Authentication authentication,HttpServletRequest request, @RequestBody SearchFilters searchFilters){
        //AuthUser loggedUser = Utils.getUserFromRequest(request,jwtToken,wholesaleUserService);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Page<WalletTransaction> transactions = walletTransactionService.getAllWalletTransactionByUserId(searchFilters, loggedUser.getId());
        return new ResponseEntity<>(transactions,HttpStatus.valueOf(200));
    }


}
