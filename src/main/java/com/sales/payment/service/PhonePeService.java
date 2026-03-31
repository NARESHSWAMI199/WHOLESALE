package com.sales.payment.service;


import com.phonepe.sdk.pg.Env;
import com.phonepe.sdk.pg.common.http.PhonePeResponse;
import com.phonepe.sdk.pg.payments.v1.PhonePePaymentClient;
import com.phonepe.sdk.pg.payments.v1.models.request.PgRefundRequest;
import com.phonepe.sdk.pg.payments.v1.models.response.PgPaymentInstrument;
import com.phonepe.sdk.pg.payments.v1.models.response.PgTransactionStatusResponse;
import com.phonepe.sdk.pg.payments.v1.models.response.UPIPaymentInstrumentResponse;
import com.sales.entities.PhonePeTrans;
import com.sales.payment.repository.PhonePeHbRepository;
import com.sales.payment.repository.PhonePeRepository;
import com.sales.request.PhonePeDto;
import com.sales.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PhonePeService {

    private static final Logger logger = LoggerFactory.getLogger(PhonePeService.class);
    private final PhonePeRepository phonePeRepository;
    private final PhonePeHbRepository phonePeHbRepository;
    @Value("${phonepe.mid}")
    public String mid;
    @Value("${phonepe.env}")
    public String phonePeEnv;
    @Value("${phonepe.key}")
    String saltKey;

    public PhonePePaymentClient getPhonePeClient() {
        logger.debug("Starting getPhonePeClient method");
        String merchentId = saltKey;
        String key = mid;
        Integer saltIndex = 1;
        Env env = Env.UAT;
        if (phonePeEnv.equals("PROD")) {
            env = Env.PROD;
        }
        boolean shouldPublishEvents = true;
        PhonePePaymentClient client = new PhonePePaymentClient(merchentId, key, saltIndex, env, shouldPublishEvents);
        logger.debug("Completed getPhonePeClient method");
        return client;
    }

    public PhonePeTrans savePhonePeTransaction(PhonePeDto phonePeDto) {
        logger.debug("Starting savePhonePeTransaction method");
        PhonePeTrans phonePeTrans = PhonePeTrans.builder()
                .merchantTransactionId(phonePeDto.getMerchantTransactionId())
                .userId(phonePeDto.getUserId())
                .amount(phonePeDto.getAmount())
                .status("P")
                .createdAt(Utils.getCurrentMillis())
                .build();
        PhonePeTrans savedTransaction = phonePeRepository.save(phonePeTrans); // Create operation
        logger.debug("Completed savePhonePeTransaction method");
        return savedTransaction;
    }

    public int updatePhonePeTransaction(PhonePeTrans phonePeTrans) {
        logger.debug("Starting updatePhonePeTransaction method");
        String responseCode = phonePeTrans.getResponseCode();
        if (responseCode.equalsIgnoreCase("SUCCESS")) {
            phonePeTrans.setStatus("S");
        } else if (responseCode.equalsIgnoreCase("ZU")) {
            phonePeTrans.setStatus("F");
        }
        int updateCount = phonePeHbRepository.updatePhonePeTransaction(phonePeTrans); // Update operation
        logger.debug("Completed updatePhonePeTransaction method");
        return updateCount;
    }

    public UPIPaymentInstrumentResponse checkUpiPaymentStatus(String merchantTransactionId) {
        logger.debug("Starting checkUpiPaymentStatus method");
        PhonePePaymentClient phonePePaymentClient = getPhonePeClient();
        PhonePeResponse<PgTransactionStatusResponse> statusResponse = phonePePaymentClient.checkStatus(merchantTransactionId);
        PgPaymentInstrument pgPaymentInstrument = statusResponse.getData().getPaymentInstrument();
        logger.debug("Completed checkUpiPaymentStatus method");
        return (UPIPaymentInstrumentResponse) pgPaymentInstrument;
    }

    public boolean checkValidityOfPaymentCallback(PhonePeDto phonePeDto, String mid) {
        logger.debug("Starting checkValidityOfPaymentCallback method");
        String xVerify = phonePeDto.getXVerify();
        String response = phonePeDto.getEncodedResponse();
        if (!Utils.isEmpty(response)) {
            if (response.contains("response")) {
                response = "{\"response\":\"" + response + "\"}";
            }
        }
        PhonePePaymentClient phonepeClient = getPhonePeClient();
        boolean isValid = phonepeClient.verifyResponse(xVerify, response);
        logger.debug("Completed checkValidityOfPaymentCallback method");
        return isValid;
    }

    public PhonePeResponse takeRefund(PhonePeDto phonePeDto, String notifyUrl) {
        logger.debug("Starting takeRefund method");
        PgRefundRequest pgRefundRequest = PgRefundRequest.builder()
                .amount(phonePeDto.getAmount() * 100)
                .callbackUrl(notifyUrl)
                .merchantId(mid)
                .merchantTransactionId(phonePeDto.getMerchantTransactionId())
                .originalTransactionId(phonePeDto.getMerchantTransactionId())
                .build();
        PhonePePaymentClient phonepeClient = getPhonePeClient();
        PhonePeResponse refundResponse = phonepeClient.refund(pgRefundRequest);
        logger.debug("Completed takeRefund method");
        return refundResponse;
    }

}
