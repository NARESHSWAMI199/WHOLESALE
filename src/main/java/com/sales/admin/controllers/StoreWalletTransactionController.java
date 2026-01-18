package com.sales.admin.controllers;


import com.sales.admin.services.StoreWalletTransactionService;
import com.sales.dto.SearchFilters;
import com.sales.entities.WalletTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/store/wallet/transactions/")
@RequiredArgsConstructor
@Tag(name = "Store Wallet Transactions", description = "APIs for managing store wallet transactions")
public class StoreWalletTransactionController  {

    private final StoreWalletTransactionService storeWalletTransactionService;

    @PreAuthorize("hasAuthority('wallet.transactions')")
    @PostMapping("all/{userSlug}")
    @Operation(summary = "Get wallet transactions by user", description = "Retrieves a paginated list of wallet transactions for a specific user")
    public ResponseEntity<Page<WalletTransaction>> getAllWalletTransactionsByUserId(@PathVariable String userSlug, HttpServletRequest request, @RequestBody SearchFilters searchFilters){
        Page<WalletTransaction> transactions = storeWalletTransactionService.getAllWalletTransactionByUserId(searchFilters, userSlug);
        return new ResponseEntity<>(transactions,HttpStatus.valueOf(200));
    }


}
