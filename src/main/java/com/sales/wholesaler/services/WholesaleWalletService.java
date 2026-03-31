package com.sales.wholesaler.services;


import com.sales.claims.AuthUser;
import com.sales.entities.ServicePlan;
import com.sales.entities.StoreNotifications;
import com.sales.entities.User;
import com.sales.entities.Wallet;
import com.sales.exceptions.NotFoundException;
import com.sales.global.ResponseMessages;
import com.sales.request.WalletTransactionRequest;
import com.sales.utils.Utils;
import com.sales.wholesaler.dto.WholesaleWalletDto;
import com.sales.wholesaler.mapper.WholesaleWalletMapper;
import com.sales.wholesaler.repository.WholesaleNotificationRepository;
import com.sales.wholesaler.repository.WholesaleServicePlanRepository;
import com.sales.wholesaler.repository.WholesaleStoreRepository;
import com.sales.wholesaler.repository.WholesaleWalletRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WholesaleWalletService {


    private static final Logger logger = LoggerFactory.getLogger(WholesaleWalletService.class);

    private final WholesaleWalletRepository wholesaleWalletRepository;
    private final WholesaleStoreRepository wholesaleStoreRepository;
    private final WholesaleServicePlanRepository wholesaleServicePlanRepository;
    private final WalletTransactionService walletTransactionService;
    private final WholesaleServicePlanService wholesaleServicePlanService;
    private final WholesaleNotificationRepository wholesaleNotificationRepository;
    private final WholesaleWalletMapper wholesaleWalletMapper;

    public WholesaleWalletDto getWalletDetail(Integer userId) {
        Wallet wallet = wholesaleWalletRepository.findByUserId(userId);
        return wholesaleWalletMapper.toDto(wallet);
    }


    @Transactional
    public void sendNotification(String title, String messageBody, int storeId, AuthUser loggedUser) {
        logger.debug("Entering sendNotification with title: {}, messageBody: {}, storeId: {}, loggedUser: {}", title, messageBody, storeId, loggedUser);
        StoreNotifications storeNotifications = StoreNotifications.builder()
                .title(title)
                .messageBody(messageBody)
                .wholesaleId(storeId)
                .createdBy(User.builder().id(loggedUser.getId()).build())
                .createAt(Utils.getCurrentMillis())
                .build();
        wholesaleNotificationRepository.save(storeNotifications);
        logger.debug("Exiting sendNotification");
    }

    public boolean paymentViaWallet(String servicePlanSlug, AuthUser loggedUser) {
        boolean payment = false;
        int userId = loggedUser.getId();
        Integer storeId = wholesaleStoreRepository.getStoreIdByUserId(userId);
        ServicePlan servicePlan = wholesaleServicePlanRepository.findBySlug(servicePlanSlug);
        if (servicePlan == null) throw new NotFoundException(ResponseMessages.SERVICE_PLAN_NOT_FOUND);
        Long planPrice = servicePlan.getPrice();
        Wallet wallet = wholesaleWalletRepository.findByUserId(userId);
        float walletAmount = wallet != null ? wallet.getAmount() : 0;

        // Preparing wallet transaction.
        WalletTransactionRequest walletTransactionDto = WalletTransactionRequest.builder()
                .amount(planPrice.floatValue())
                .transactionType("DR")
                .status("F") // Default assuming it failed.
                .build();

        String title = null;
        String messageBody = null;
        if (wallet != null && walletAmount >= planPrice) {
            wallet.setAmount(walletAmount - planPrice); // Updating wallet amount.
            wholesaleWalletRepository.save(wallet);
            wholesaleServicePlanService.assignOrAddFuturePlans(userId, servicePlan.getId());
            title = "Payment of " + planPrice;
            messageBody = "Your plan activated successfully. Plan Name : " + servicePlan.getName() + " for " + servicePlan.getMonths() + "Months.";
            walletTransactionDto.setStatus("S"); // Payment success.
            payment = true;
        } else {
            title = "Payment of " + planPrice;
            messageBody = "Wallet amount is inefficient for this payment.";
        }

        // Updating wallet transactions.
        walletTransactionService.addWalletTransaction(walletTransactionDto, userId);
        // Send notification also
        sendNotification(title, messageBody, storeId, loggedUser);
        return payment;
    }


}
