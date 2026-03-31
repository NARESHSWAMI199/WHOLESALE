package com.sales.specifications;


import com.sales.entities.WalletTransaction;
import com.sales.entities.WalletTransaction_;
import com.sales.utils.Utils;
import org.springframework.data.jpa.domain.Specification;

public class WalletTransactionSpecification {


    public static Specification<WalletTransaction> hasSlug(String slug) {
        return ((root, query, criteriaBuilder) -> {
            if (Utils.isEmpty(slug)) return null;
            return criteriaBuilder.equal(root.get(WalletTransaction_.slug), slug);
        });
    }

    public static Specification<WalletTransaction> hasUserId(Integer userId) {
        return ((root, query, criteriaBuilder) -> {
            if (userId == null || userId.equals(0)) return null;
            return criteriaBuilder.equal(root.get(WalletTransaction_.userId), userId);
        });
    }


    public static Specification<WalletTransaction> hasSlug(Float amount) {
        return ((root, query, criteriaBuilder) -> {
            if (amount == null || amount == 0) return null;
            return criteriaBuilder.equal(root.get(WalletTransaction_.amount), amount);
        });
    }


    public static Specification<WalletTransaction> greaterThanOrEqualFromDate(Long fromDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get(WalletTransaction_.CREATED_AT), fromDate);
        };
    }

    public static Specification<WalletTransaction> lessThanOrEqualToToDate(Long toDate) {
        return (root, query, criteriaBuilder) -> {
            if (toDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(WalletTransaction_.CREATED_AT), toDate);
        };
    }


}
