package com.sales.specifications;

import com.sales.entities.User;
import com.sales.entities.User_;
import com.sales.entities.WholesalerPlans;
import com.sales.entities.WholesalerPlans_;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class PlansSpecifications {


    /**
     * there we create some predicates that are use for filters
     */


    public static Specification<User> containsName(String searchKey) {
        return (root, query, criteriaBuilder) -> {
            if (searchKey == null) return null;
            return criteriaBuilder.like(root.get(User_.USERNAME), "%" + searchKey + "%");
        };
    }


    public static Specification<WholesalerPlans> greaterThanOrEqualCreatedFromDate(Long createdFromDate) {
        return (root, query, criteriaBuilder) -> {
            if (createdFromDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get(WholesalerPlans_.CREATED_AT), createdFromDate);
        };
    }

    public static Specification<WholesalerPlans> lessThanOrEqualToCreatedToDate(Long createdToDate) {
        return (root, query, criteriaBuilder) -> {
            if (createdToDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(WholesalerPlans_.CREATED_AT), createdToDate);
        };
    }


    public static Specification<WholesalerPlans> greaterThanOrEqualExpiredFromDate(Long expiredFromDate) {
        return (root, query, criteriaBuilder) -> {
            if (expiredFromDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get(WholesalerPlans_.EXPIRY_DATE), expiredFromDate);
        };
    }

    public static Specification<WholesalerPlans> lessThanOrEqualToExpiredToDate(Long expiredToDate) {
        return (root, query, criteriaBuilder) -> {
            if (expiredToDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(WholesalerPlans_.EXPIRY_DATE), expiredToDate);
        };
    }


    /* Expired or not */
    public static Specification<WholesalerPlans> isStatus(String status) {
        List<String> statusList = List.of("A", "D");
        return (root, query, criteriaBuilder) -> {
            if (status == null || !statusList.contains(status)) {
                return null;
            } else if (status.equals("A")) {
                return criteriaBuilder.lessThanOrEqualTo(root.get(WholesalerPlans_.createdAt), root.get(WholesalerPlans_.expiryDate));
            }
            return criteriaBuilder.greaterThan(root.get(WholesalerPlans_.createdAt), root.get(WholesalerPlans_.expiryDate));
        };
    }

    public static Specification<WholesalerPlans> hasSlug(String slug) {
        return (root, query, criteriaBuilder) -> {
            if (slug == null || slug.isEmpty()) return null;
            return criteriaBuilder.equal(root.get(WholesalerPlans_.SLUG), slug.trim());
        };
    }

    public static Specification<WholesalerPlans> isUserId(Integer userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) return null;
            return criteriaBuilder.equal(root.get(WholesalerPlans_.USER_ID), userId);
        };
    }

}
