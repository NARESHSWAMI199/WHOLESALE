package com.sales.admin.services;


import com.sales.admin.dto.WalletDto;
import com.sales.admin.mapper.WalletMapper;
import com.sales.admin.repositories.StoreHbRepository;
import com.sales.admin.repositories.StoreRepository;
import com.sales.admin.repositories.UserRepository;
import com.sales.admin.repositories.WalletRepository;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.global.ResponseMessages;
import com.sales.request.WalletTransactionRequest;
import com.sales.entities.ServicePlan;
import com.sales.entities.StoreNotifications;
import com.sales.entities.User;
import com.sales.entities.Wallet;
import com.sales.exceptions.NotFoundException;
import com.sales.wholesaler.services.WalletTransactionService;
import com.sales.wholesaler.services.WholesaleServicePlanService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final StoreHbRepository storeHbRepository;
    private final StoreRepository storeRepository;
    private final ServicePlanService servicePlanService;
    private final WholesaleServicePlanService wholesaleServicePlanService;
    private final WalletTransactionService walletTransactionService;
    private final WalletMapper walletMapper;
    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    @Transactional(readOnly = true)
    public WalletDto getWalletDetail(String userSlug){
        Integer userId = userRepository.getUserIdBySlug(userSlug);
        if (userId == null) throw new NotFoundException(ResponseMessages.WALLET_USER_DETAILS_NOT_FOUND);
        Wallet wallet = walletRepository.findByUserId(userId);
        return walletMapper.toDto(wallet);
    }


    @Transactional
    public void sendNotification(String title, String messageBody, int storeId, AuthUser loggedUser){
        logger.debug("Entering sendNotification with title: {}, messageBody: {}, storeId: {}, loggedUser: {}", title, messageBody, storeId, loggedUser);
        StoreNotifications storeNotifications = new StoreNotifications();
        storeNotifications.setTitle(title);
        storeNotifications.setMessageBody(messageBody);
        storeNotifications.setWholesaleId(storeId);
        storeNotifications.setCreatedBy(User.builder().id(loggedUser.getId()).build());
        storeHbRepository.insertStoreNotifications(storeNotifications);
        logger.debug("Exiting sendNotification");
    }

    public boolean paymentViaWallet(String servicePlanSlug, String slug) {
        boolean payment = false;
        User user = userRepository.findUserBySlug(slug);
        if(user == null) throw new NotFoundException(ResponseMessages.USER_NOT_FOUND);
        ServicePlan servicePlan = servicePlanService.findBySlug(servicePlanSlug);
        if(servicePlan == null) throw new NotFoundException(ResponseMessages.SERVICE_PLAN_NOT_FOUND);
        Long planPrice = servicePlan.getPrice();
        Wallet wallet = walletRepository.findByUserId(user.getId());
        float walletAmount = wallet != null ? wallet.getAmount() : 0;

        // Preparing wallet transaction.
        WalletTransactionRequest walletTransactionDto = WalletTransactionRequest.builder()
                .amount(planPrice.floatValue())
                .transactionType("DR")
                .status("F") // Default assuming it failed.
                .build();

        String title = null;
        String messageBody = null;
        if(wallet != null && walletAmount >= planPrice){
            wallet.setAmount(walletAmount-planPrice); // Updating wallet amount.
            walletRepository.save(wallet);
            wholesaleServicePlanService.assignOrAddFuturePlans(user.getId(),servicePlan.getId());
            title = "Payment of "+planPrice;
            messageBody = "Your plan activated successfully. Plan Name : "+servicePlan.getName() + " for "+servicePlan.getMonths() + "Months.";
            walletTransactionDto.setStatus("S"); // Payment success.
            payment = true;
        }else{
            title = "Payment of "+planPrice;
            messageBody = "Wallet amount is inefficient for this payment.";
        }

        // Updating wallet transactions.
        walletTransactionService.addWalletTransaction(walletTransactionDto,user.getId());
        // Send notification also
        Integer storeId = storeRepository.getStoreIdByUserId(user.getId());
        sendNotification(title,messageBody,storeId,new SalesUser(user));
        return payment;
    }



}
