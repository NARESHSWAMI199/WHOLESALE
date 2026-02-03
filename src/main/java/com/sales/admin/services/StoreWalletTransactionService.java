package com.sales.admin.services;

import com.sales.admin.repositories.StoreWalletTransactionRepository;
import com.sales.admin.repositories.UserRepository;
import com.sales.admin.repositories.WalletRepository;
import com.sales.dto.SearchFilters;
import com.sales.dto.WalletTransactionRequest;
import com.sales.entities.Wallet;
import com.sales.entities.WalletTransaction;
import com.sales.exceptions.NotFoundException;
import com.sales.utils.Utils;
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
public class StoreWalletTransactionService {


    private final UserRepository userRepository;
    private final StoreWalletTransactionRepository storeWalletTransactionRepository;
    private final WalletRepository walletRepository;

      
  private static final Logger logger = LoggerFactory.getLogger(StoreWalletTransactionService.class);




    public Page<WalletTransaction> getAllWalletTransactionByUserId(SearchFilters searchFilters,String userSlug){
        Integer userId = userRepository.getUserIdBySlug(userSlug);
        if (userId == null) throw new NotFoundException("User not found.");
        Specification<WalletTransaction> specification = Specification.allOf(
            hasSlug(searchFilters.getSlug())
            .and(greaterThanOrEqualFromDate(searchFilters.getFromDate()))
            .and(lessThanOrEqualToToDate(searchFilters.getToDate())
            .and(hasUserId(userId))
        ));
        Pageable pageable = getPageable(logger,searchFilters);
        return storeWalletTransactionRepository.findAll(specification,pageable);
    }



    public WalletTransaction addWalletTransaction(WalletTransactionRequest walletTransactionDto,Integer userId) {
        logger.debug("The addWalletTransaction method started with wallTransactionDto : {}",walletTransactionDto);
        WalletTransaction walletTransaction = WalletTransaction.builder()
                .slug(UUID.randomUUID().toString())
                .userId(userId)
                .amount(walletTransactionDto.getAmount())
                .transactionType(walletTransactionDto.getTransactionType())
                .createdAt(Utils.getCurrentMillis())
                .status(walletTransactionDto.getStatus())
                .build();
        logger.debug("The addWalletTransaction method ended with wallTransactionDto : {}",walletTransaction);

        if(!Utils.isEmpty(walletTransactionDto.getTransactionType()) && walletTransactionDto.getTransactionType().equalsIgnoreCase("CR")){
            Integer added = walletRepository.addMoneyInWallet(walletTransaction.getAmount(), userId, Utils.getCurrentMillis());
            logger.debug("Added money in wallet rows was updated : {} ",added);
            if(added < 1){ // if a user isn't found in wallet.
                Wallet wallet = Wallet.builder()
                        .userId(userId)
                        .amount(walletTransaction.getAmount())
                        .updatedAt(Utils.getCurrentMillis())
                        .build();
                walletRepository.save(wallet);
            }
        }else{
            Integer deducted = walletRepository.deductMoneyFromWallet(walletTransaction.getAmount(), userId, Utils.getCurrentMillis());
            logger.debug("Detected money in wallet rows was updated : {} ",deducted);
        }

        return storeWalletTransactionRepository.save(walletTransaction);
    }

}
