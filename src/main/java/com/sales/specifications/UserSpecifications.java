package com.sales.specifications;

import com.sales.entities.User;
import com.sales.entities.User_;
import com.sales.global.GlobalConstant;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class UserSpecifications {


    /**
     * there we create some predicates which is use for filters
     */


    public static Specification<User> hasUserType(String userType) {
        return (root, query, criteriaBuilder) -> {
            if (userType == null) return null;
            return criteriaBuilder.equal(root.get(User_.USER_TYPE), userType);
        };
    }

    public static Specification<User> hasNotUserType(String userType) {
        return (root, query, criteriaBuilder) -> {
            if (userType == null) return null;
            return criteriaBuilder.notEqual(root.get(User_.USER_TYPE), userType);
        };
    }


    public static Specification<User> isUserId(Integer wholesaleId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(User_.ID), wholesaleId);
    }


    public static Specification<User> containsName(String searchKey) {
        return (root, query, criteriaBuilder) -> {
            if (searchKey == null) return null;
            return criteriaBuilder.like(root.get(User_.USERNAME), "%" + searchKey + "%");
        };
    }

    public static Specification<User> containsEmail(String searchKey) {
        return (root, query, criteriaBuilder) -> {
            if (searchKey == null) return null;
            return criteriaBuilder.like(root.get(User_.EMAIL), "%" + searchKey + "%");
        };
    }

    public static Specification<User> greaterThanOrEqualFromDate(Long fromDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get(User_.CREATED_AT), fromDate);
        };
    }

    public static Specification<User> lessThanOrEqualToToDate(Long toDate) {
        return (root, query, criteriaBuilder) -> {
            if (toDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(User_.CREATED_AT), toDate);
        };
    }


    public static Specification<User> isStatus(String status) {
        List<String> statusList = List.of("A", "D");
        return (root, query, criteriaBuilder) -> {
            if (status == null || !statusList.contains(status)) return null;
            return criteriaBuilder.equal(root.get(User_.STATUS), status);
        };
    }

    public static Specification<User> hasSlug(String slug) {
        return (root, query, criteriaBuilder) -> {
            if (slug == null || slug.isEmpty()) return null;
            return criteriaBuilder.equal(root.get(User_.SLUG), slug.trim());
        };
    }

    public static Specification<User> notHasSlug(String slug) {
        return (root, query, criteriaBuilder) -> {
            if (slug == null || slug.isEmpty()) return null;
            return criteriaBuilder.notEqual(root.get(User_.SLUG), slug.trim());
        };
    }

    public static Specification<User> notSuperAdmin() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(User_.id), GlobalConstant.suId);
    }


}
