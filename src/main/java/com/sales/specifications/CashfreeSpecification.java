package com.sales.specifications;


import com.sales.entities.CashfreeTrans;
import com.sales.entities.CashfreeTrans_;
import com.sales.utils.Utils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CashfreeSpecification {


    public static Specification<CashfreeTrans> hasCfPaymentId(String cfPaymentId) {
        return ((root, query, criteriaBuilder) -> {
            if (Utils.isEmpty(cfPaymentId)) return null;
            return criteriaBuilder.equal(root.get(CashfreeTrans_.CF_PAYMENT_ID), cfPaymentId);
        });
    }

    public static Specification<CashfreeTrans> hasPaymentStatus(String status) {
        return ((root, query, criteriaBuilder) -> {
            if (Utils.isEmpty(status)) return null;
            return criteriaBuilder.equal(root.get(CashfreeTrans_.STATUS), status);
        });
    }


    public static Specification<CashfreeTrans> greaterThanOrEqualFromDate(Long fromDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get(CashfreeTrans_.CREATED_AT), fromDate);
        };
    }

    public static Specification<CashfreeTrans> lessThanOrEqualToToDate(Long toDate) {
        return (root, query, criteriaBuilder) -> {
            if (toDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(CashfreeTrans_.CREATED_AT), toDate);
        };
    }


}
