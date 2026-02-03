package com.sales.payment.repository;


import com.sales.dto.CashfreeRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor
public class CashfreeHbRepository {

    private final EntityManager entityManager;

    public int updateCashfreePaymentDetail(CashfreeRequest cashfreeRequest, Integer userId){
        String hql = """
            update CashfreeTrans
            set
                userId = :userId,
                orderId = :orderId,
                amount = :amount,
                cfPaymentId = :cfPaymentId,
                status = :status,
                currency = :currency,
                message = :message,
                paymentTime = :paymentTime,
                paymentType = :paymentType,
                paymentMethod = :paymentMethod,
                actualResponse = :actualResponse
            where slug = :slug
        """;
        Query query = entityManager.createQuery(hql);
        query.setParameter("userId",String.valueOf(userId));
        query.setParameter("orderId",String.valueOf(cashfreeRequest.getOrderId()));
        query.setParameter("amount",String.valueOf(cashfreeRequest.getAmount()));
        query.setParameter("cfPaymentId",cashfreeRequest.getCfPaymentId());
        query.setParameter("status",cashfreeRequest.getStatus());
        query.setParameter("currency",cashfreeRequest.getCurrency());
        query.setParameter("message",cashfreeRequest.getMessage());
        query.setParameter("paymentTime",cashfreeRequest.getPaymentTime());
        query.setParameter("paymentType",cashfreeRequest.getPaymentType());
        query.setParameter("paymentMethod",cashfreeRequest.getPaymentMethod());
        query.setParameter("actualResponse",cashfreeRequest.getActualResponse());
        query.setParameter("slug",cashfreeRequest.getSlug());
        return query.executeUpdate();
    }


}
