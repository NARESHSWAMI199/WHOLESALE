package com.sales.payment.service;


import com.cashfree.ApiException;
import com.cashfree.ApiResponse;
import com.cashfree.Cashfree;
import com.cashfree.model.CreateOrderRequest;
import com.cashfree.model.CustomerDetails;
import com.cashfree.model.OrderEntity;
import com.cashfree.model.OrderMeta;
import com.sales.claims.AuthUser;
import com.sales.request.CashfreeRequest;
import com.sales.request.CashfreeFilters;
import com.sales.entities.CashfreeTrans;
import com.sales.entities.ServicePlan;
import com.sales.global.GlobalConstant;
import com.sales.payment.controller.CashFreePgController;
import com.sales.payment.repository.CashfreeHbRepository;
import com.sales.payment.repository.CashfreeRepository;
import com.sales.utils.Utils;
import com.sales.wholesaler.services.WholesaleServicePlanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.sales.helpers.PaginationHelper.getPageable;
import static com.sales.specifications.CashfreeSpecification.*;

@Service
@RequiredArgsConstructor
public class CashfreeService {

    private final CashfreeHbRepository cashfreeHbRepository;
    private final CashfreeRepository cashfreeRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(CashFreePgController.class);


    @Value("${cashfree.key}")
    String key;

    @Value("${cashfree.mid}")
    public String mid;

    @Value("${cashfree.mobile}")
    String mobileNumber;

    @Value("${cashfree.redirect_uri}")
    String redirectUri;

    @Value("${cashfree.callback_uri}")
    String callbackUri;

    private final WholesaleServicePlanService wholesaleServicePlanService;

    public Page<CashfreeTrans> getAllPaymentHistoryFromCashfree(CashfreeFilters cashfreeFilters) {
        Specification<CashfreeTrans> specification = Specification.allOf(
                hasCfPaymentId(cashfreeFilters.getTransactionId())
                        .and(hasPaymentStatus(cashfreeFilters.getPaymentStatus()))
                        .and(greaterThanOrEqualFromDate(cashfreeFilters.getFromDate()))
                        .and(lessThanOrEqualToToDate(cashfreeFilters.getToDate()))
        );
        Pageable pageable = getPageable(logger,cashfreeFilters);
        return cashfreeRepository.findAll(specification, pageable);
    }


    public void insertPaymentDetail(CashfreeRequest cashfreeRequest) {
        CashfreeTrans cashfreeTrans = CashfreeTrans.builder()
                .orderId(cashfreeRequest.getOrderId())
                .amount(String.valueOf(cashfreeRequest.getAmount()))
                .status(cashfreeRequest.getStatus())
                .currency(cashfreeRequest.getCurrency())
                .slug(cashfreeRequest.getSlug())
                .amount(String.valueOf(cashfreeRequest.getAmount()))
                .userId(cashfreeRequest.getUserId())
                .createdAt(Utils.getCurrentMillis())
                .build();
        cashfreeRepository.save(cashfreeTrans);
    }


    public int updateCashfreeCallback(JSONObject order, JSONObject payment, String slug, Integer userId, Integer servicePlanId, String paymentResponseStr) {
        logger.debug("started updateCashfreeCallback with params order : {} payment : {} slug : {} userId : {} servicePlanId : {} ", order, payment, slug, userId, servicePlanId);
        String orderId = String.valueOf(order.get("order_id"));
        String cfPaymentId = String.valueOf(payment.get("cf_payment_id"));
        String paymentStatus = payment.getString("payment_status");
        String paymentAmount = String.valueOf(payment.get("payment_amount"));
        String paymentCurrency = payment.getString("payment_currency");
        String paymentMessage = payment.getString("payment_message");
        String paymentTime = String.valueOf(payment.get("payment_time"));
        String bankReference = String.valueOf(payment.get("bank_reference"));
        String paymentGroup = payment.getString("payment_group");
        String paymentMethod = payment.getJSONObject("payment_method").toString();

        CashfreeRequest cashfreeRequest = CashfreeRequest.builder()
                .orderId(orderId)
                .slug(slug)
                .cfPaymentId(cfPaymentId)
                .status(paymentStatus)
                .amount(Double.parseDouble(paymentAmount))
                .currency(paymentCurrency)
                .message(paymentMessage)
                .paymentTime(paymentTime)
                .bankReference(bankReference)
                .paymentType(paymentGroup)
                .paymentMethod(paymentMethod)
                .actualResponse(paymentResponseStr)
                .build();

        int updatedRows = cashfreeHbRepository.updateCashfreePaymentDetail(cashfreeRequest, userId);
        logger.debug("Updated rows in updateCashfreeCallback. return by updatePaymentCallback -> {}", updatedRows);
        // The Active plan is payment status is successful
        if (paymentStatus.equals("SUCCESS")) wholesaleServicePlanService.assignOrAddFuturePlans(userId, servicePlanId);
        logger.debug("Ended updateCashfreeCallback.");
        return updatedRows;
    }


    public OrderEntity getOrderEntityForCashfreePaymentForPlans(HttpServletRequest httpServletRequest, CashfreeRequest cashfreeRequest, AuthUser loggedUser, ServicePlan servicePlan, String givenRedirectUri, String env) throws ApiException {
        logger.debug("Started getOrderEntityForCashfreePaymentForPlans with params : cashfreeRequest : {} and loggedUser : {} and servicePlan : {} and redirectUri : {} and env : {}", cashfreeRequest, loggedUser, servicePlan, givenRedirectUri, env);
        long amount = (servicePlan.getPrice() - servicePlan.getDiscount());
        String slug = UUID.randomUUID().toString();
        logger.debug("amount {}", amount);
        Cashfree.XClientId = mid;
        Cashfree.XClientSecret = key;
        if (env != null && env.equalsIgnoreCase("TEST")) {
            Cashfree.XEnvironment = Cashfree.SANDBOX;
        } else {
            Cashfree.XEnvironment = Cashfree.PRODUCTION;
        }
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setCustomerId(UUID.randomUUID().toString());
        customerDetails.setCustomerPhone(mobileNumber);

        CreateOrderRequest request = new CreateOrderRequest();
        OrderMeta orderMeta = new OrderMeta();

        // we redirect on referred site.
        if (givenRedirectUri == null || givenRedirectUri.trim().isEmpty()){
            orderMeta.setReturnUrl(redirectUri+"/?congratulation="+servicePlan.getSlug());
        }else {
            orderMeta.setReturnUrl(givenRedirectUri+"?congratulation="+servicePlan.getSlug());
        }
        // Updating callback uri if not provided.
        if(callbackUri == null) callbackUri = httpServletRequest.getRequestURI();
        orderMeta.setNotifyUrl(callbackUri+slug+ GlobalConstant.PATH_SEPARATOR+loggedUser.getId()+GlobalConstant.PATH_SEPARATOR+servicePlan.getId());
        request.setOrderMeta(orderMeta);
        request.setOrderAmount((double) amount);
        request.setOrderCurrency("INR");
        request.setCustomerDetails(customerDetails);
        Cashfree cashfree = new Cashfree();
        ApiResponse<OrderEntity> response = cashfree.PGCreateOrder("2023-08-01", request, null, null, null);
        logger.debug("Payment session ID generated successfully: {}", response.getData().getPaymentSessionId());
        logger.debug("The order id for payment : {}",response.getData().getOrderId());

        cashfreeRequest.setSlug(slug);
        cashfreeRequest.setAmount((double) amount);
        cashfreeRequest.setCurrency("INR");
        cashfreeRequest.setOrderId(response.getData().getOrderId());
        cashfreeRequest.setStatus("VISITED");
        cashfreeRequest.setUserId(loggedUser.getId());
        insertPaymentDetail(cashfreeRequest);
        logger.debug("Ended getOrderEntityForCashfreePaymentForPlans with -> {}",response.getData());
        return response.getData();
    }




    public OrderEntity getOrderEntityForCashfreePaymentForWallet(HttpServletRequest httpServletRequest, CashfreeRequest cashfreeRequest, AuthUser loggedUser, Double amount, String givenRedirectUri, String env) throws ApiException {
        logger.debug("Started getOrderEntityForCashfreePaymentForWallet with params : cashfreeRequest : {} and loggedUser : {} and redirectUri : {} and env : {}", cashfreeRequest.toString(), loggedUser.toString(), givenRedirectUri, env);

        String slug = UUID.randomUUID().toString();
        logger.debug("amount {}", amount);
        Cashfree.XClientId = mid;
        Cashfree.XClientSecret = key;
        if (env != null && env.equalsIgnoreCase("TEST")) {
            Cashfree.XEnvironment = Cashfree.SANDBOX;
        } else {
            Cashfree.XEnvironment = Cashfree.PRODUCTION;
        }
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setCustomerId(UUID.randomUUID().toString());
        customerDetails.setCustomerPhone(mobileNumber);

        CreateOrderRequest request = new CreateOrderRequest();
        OrderMeta orderMeta = new OrderMeta();

        // we redirect on referred site.
        if (givenRedirectUri == null || givenRedirectUri.trim().isEmpty()){
            orderMeta.setReturnUrl(redirectUri);
        }else {
            orderMeta.setReturnUrl(givenRedirectUri);
        }
        // Updating callback uri if not provided.
        if(callbackUri == null) callbackUri = httpServletRequest.getRequestURI();
        orderMeta.setNotifyUrl(callbackUri+slug+GlobalConstant.PATH_SEPARATOR+loggedUser.getId());
        request.setOrderMeta(orderMeta);
        request.setOrderAmount(amount);
        request.setOrderCurrency("INR");
        request.setCustomerDetails(customerDetails);
        Cashfree cashfree = new Cashfree();
        ApiResponse<OrderEntity> response = cashfree.PGCreateOrder("2023-08-01", request, null, null, null);
        logger.debug("Payment session ID generated successfully: {}", response.getData().getPaymentSessionId());
        logger.debug("The order id for payment : {}",response.getData().getOrderId());

        cashfreeRequest.setSlug(slug);
        cashfreeRequest.setAmount((double) amount);
        cashfreeRequest.setCurrency("INR");
        cashfreeRequest.setOrderId(response.getData().getOrderId());
        cashfreeRequest.setStatus("VISITED");
        cashfreeRequest.setUserId(loggedUser.getId());
        insertPaymentDetail(cashfreeRequest);
        logger.debug("Ended getOrderEntityForCashfreePaymentForWallet with -> {}",response.getData());
        return response.getData();
    }





}
