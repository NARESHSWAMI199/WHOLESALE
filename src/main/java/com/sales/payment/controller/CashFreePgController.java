package com.sales.payment.controller;


import com.cashfree.ApiException;
import com.cashfree.model.OrderEntity;
import com.google.gson.Gson;
import com.sales.admin.services.ServicePlanService;
import com.sales.request.CashfreeRequest;
import com.sales.request.WalletTransactionRequest;
import com.sales.entities.ServicePlan;
import com.sales.entities.User;
import com.sales.entities.WalletTransaction;
import com.sales.exceptions.NotFoundException;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.ResponseMessages;
import com.sales.jwtUtils.JwtToken;
import com.sales.payment.service.CashfreeService;
import com.sales.utils.Utils;
import com.sales.wholesaler.services.WalletTransactionService;
import com.sales.wholesaler.services.WholesaleUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("cashfree")
@RequiredArgsConstructor
public class CashFreePgController {

    private final WholesaleUserService wholesaleUserService;
    private final ServicePlanService servicePlanService;
    private final CashfreeService cashfreeService;
    private final JwtToken jwtToken;
    private final WalletTransactionService walletTransactionService;
    
    private static final Logger logger = LoggerFactory.getLogger(CashFreePgController.class);

    @Value("${cashfree.env}")
    String env;

    @ResponseBody
    @PostMapping(value = {"sessionId/{paymentFor}"})
    public ResponseEntity<Map<String,Object>> getPaymentSessionId (HttpServletRequest request,@RequestParam(value = "redirectUri", required = false) String redirectUri,
                                                                   @RequestBody CashfreeRequest cashfreeRequest,@PathVariable String paymentFor) {
        User loggedUser = wholesaleUserService.findUserBySlug(cashfreeRequest.getUserSlug());
        if(loggedUser == null) throw new NotFoundException("No logged user found.");
        Map<String,Object> result = new HashMap<>();
        OrderEntity orderEntity = null;
            try {
                if(!paymentFor.equalsIgnoreCase("wallet")) {
                    logger.debug("Received request to get payment session ID for service slug : {} and username : {} and user slug : {}", cashfreeRequest.getServicePlanSlug(), loggedUser.getUsername(), loggedUser.getSlug());
                    ServicePlan servicePlan = servicePlanService.findBySlug(cashfreeRequest.getServicePlanSlug());
                    if (servicePlan == null) throw new NotFoundException("No service plan found.");
                    orderEntity = cashfreeService.getOrderEntityForCashfreePaymentForPlans(request, cashfreeRequest, loggedUser, servicePlan, redirectUri, env);
                }else {
                    orderEntity = cashfreeService.getOrderEntityForCashfreePaymentForWallet(request, cashfreeRequest, loggedUser,cashfreeRequest.getAmount(), redirectUri, env);
                }
                result.put(ConstantResponseKeys.RES, orderEntity);
                result.put(ConstantResponseKeys.STATUS, 201);
            } catch (ApiException e) {
                logger.error("Exception occurred while getting payment session ID : {}", e.getMessage());
                result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SOMETHING_WENT_WRONG_DURING_GETPAYMENTSESSIONID_PAYMENT_PLEASE_CONTACT_TO_ADMINISTRATOR);
                result.put(ConstantResponseKeys.STATUS, 500);
                logger.debug("Exception occur in  getPaymentSessionId :: {}", e.getMessage());
            }
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get("status")));
    }

    @GetMapping(value = {"pay/{servicePlanSlug}/{token}","pay/{token}"})
    public String redirectPaymentPage(HttpServletRequest request,@PathVariable(required = false) String servicePlanSlug, @PathVariable String token,@RequestParam(required = false) Float amount,
                                      @RequestParam(value = "redirectUri", required = false) String redirectUri, Model model) {
        User loggedUser = Utils.getUserFromRequest(request,token,jwtToken,wholesaleUserService);
        logger.debug("Redirecting to payment page for servicePlanSlug : {} user : {} and user slug : {} and redirect uri : {} and ", servicePlanSlug,loggedUser.getUsername(),loggedUser.getSlug(),redirectUri);
        model.addAttribute("servicePlanSlug",servicePlanSlug);
        model.addAttribute("amount",amount);
        model.addAttribute("userSlug",loggedUser.getSlug());
        model.addAttribute("env",env);
        model.addAttribute("redirectUri",redirectUri);
        return "cashfree";
    }



    @PostMapping(value={"callback/{slug}/{userId}/{servicePlanId}","callback/{slug}/{userId}"})
    public synchronized ResponseEntity<Map<String,Object>> saveCashfreeCallback(@PathVariable String slug, @PathVariable Integer userId,
                                                                   @PathVariable(required = false)  Integer servicePlanId, @RequestBody Map<String,Object> data) {
        Map<String,Object> result = new HashMap<>();
        try {
            logger.debug("Response getting from callback  : {} ", data.toString());
            String paymentResponseStr = new Gson().toJson(data.get("data"));
            JSONObject paymentDetails = new JSONObject(paymentResponseStr);
            JSONObject order = null;
            JSONObject payment = null;

            if (paymentDetails.has("order")) order = paymentDetails.getJSONObject("order");
            if (paymentDetails.has("payment")) payment = paymentDetails.getJSONObject("payment");

            assert order != null;
            assert payment != null;
            if(servicePlanId != null) {
                int isUpdated = cashfreeService.updateCashfreeCallback(order, payment, slug, userId, servicePlanId, paymentResponseStr);
                logger.debug("Cashfree callback processed successfully for user: {} and status isUpdated : {}",userId,isUpdated);
                result.put("isUpdate", isUpdated > 0);
            }else{
                Float amount =  Float.valueOf(String.valueOf(payment.get("payment_amount")));
                String paymentStatus = payment.getString("payment_status");
                paymentStatus = paymentStatus.equals("SUCCESS") ? "S" : "F";
                WalletTransactionRequest walletTransactionDto = WalletTransactionRequest.builder()
                        .userId(userId)
                        .amount(amount)
                        .transactionType("CR")
                        .status(paymentStatus)
                        .build();
                WalletTransaction walletTransaction = walletTransactionService.addWalletTransaction(walletTransactionDto,userId);
                logger.debug("Cashfree callback processed successfully for user: {} and walletTransaction : {}",userId,walletTransaction);
            }
            result.put("response", data);
            result.put(ConstantResponseKeys.STATUS, 200);
        }catch (Exception e){
            logger.error("Exception during cashfree callback : {}",e.getMessage());
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SOMETHING_WENT_WRONG_DURING_CASHFREE_CALLBACK_PLEASE_CONTACT_TO_ADMINISTRATOR);
            result.put(ConstantResponseKeys.STATUS,500);
        }
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get("status")));
    }



}
