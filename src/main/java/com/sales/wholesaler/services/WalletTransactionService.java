package com.sales.wholesaler.services;

import com.sales.entities.Wallet;
import com.sales.entities.WalletTransaction;
import com.sales.request.WalletFilterRequest;
import com.sales.request.WalletTransactionRequest;
import com.sales.utils.Utils;
import com.sales.wholesaler.dto.WholesaleWalletTransactionDto;
import com.sales.wholesaler.repository.WalletTransactionHbRepository;
import com.sales.wholesaler.repository.WalletTransactionRepository;
import com.sales.wholesaler.repository.WholesaleWalletRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.sales.helpers.PaginationHelper.getPageable;
import static com.sales.specifications.WalletTransactionSpecification.*;

@Service
@RequiredArgsConstructor
public class WalletTransactionService {


    private static final Logger logger = LoggerFactory.getLogger(WalletTransactionService.class);
    private final WalletTransactionHbRepository WalletTransactionHbRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WholesaleWalletRepository wholesaleWalletRepository;


    public Page<WholesaleWalletTransactionDto> getAllWalletTransactionByUserId(WalletFilterRequest searchFilters, Integer userId) {
        Specification<WalletTransaction> specification = Specification.allOf(
                hasSlug(searchFilters.getSlug())
                        .and(greaterThanOrEqualFromDate(searchFilters.getFromDate()))
                        .and(lessThanOrEqualToToDate(searchFilters.getToDate())
                                .and(hasUserId(userId))
                        ));
        Pageable pageable = getPageable(logger, searchFilters);
        return WalletTransactionHbRepository.findAll(specification, pageable);
    }


    public WalletTransaction addWalletTransaction(WalletTransactionRequest walletTransactionDto, Integer userId) {
        logger.debug("The addWalletTransaction method started with wallTransactionDto : {}", walletTransactionDto);
        WalletTransaction walletTransaction = WalletTransaction.builder()
                .slug(UUID.randomUUID().toString())
                .userId(userId)
                .amount(walletTransactionDto.getAmount())
                .transactionType(walletTransactionDto.getTransactionType())
                .createdAt(Utils.getCurrentMillis())
                .status(walletTransactionDto.getStatus())
                .build();
        logger.debug("The addWalletTransaction method ended with wallTransactionDto : {}", walletTransaction);

        if (!Utils.isEmpty(walletTransactionDto.getTransactionType()) && walletTransactionDto.getTransactionType().equalsIgnoreCase("CR")) {
            Integer added = wholesaleWalletRepository.addMoneyInWallet(walletTransaction.getAmount(), userId, Utils.getCurrentMillis());
            logger.debug("Added money in wallet rows was updated : {} ", added);
            if (added < 1) { // if a user isn't found in wallet.
                Wallet wallet = Wallet.builder()
                        .userId(userId)
                        .amount(walletTransaction.getAmount())
                        .updatedAt(Utils.getCurrentMillis())
                        .build();
                wholesaleWalletRepository.save(wallet);
            }
        } else {
            Integer deducted = wholesaleWalletRepository.deductMoneyFromWallet(walletTransaction.getAmount(), userId, Utils.getCurrentMillis());
            logger.debug("Detected money in wallet rows was updated : {} ", deducted);
        }

        return walletTransactionRepository.save(walletTransaction);
    }

}
