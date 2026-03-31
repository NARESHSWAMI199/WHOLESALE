package com.sales.specifications;

import com.sales.entities.Store;
import com.sales.entities.Store_;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class StoreSpecifications {


    public static Specification<Store> isStoreId(Integer wholesaleId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Store_.ID), wholesaleId);
    }


    public static Specification<Store> containsName(String searchKey) {
        return (root, query, criteriaBuilder) -> {
            if (searchKey == null) return null;
            return criteriaBuilder.like(root.get(Store_.STORE_NAME), "%" + searchKey + "%");
        };
    }

    public static Specification<Store> containsEmail(String searchKey) {
        return (root, query, criteriaBuilder) -> {
            if (searchKey == null) return null;
            return criteriaBuilder.like(root.get(Store_.EMAIL), "%" + searchKey + "%");
        };
    }

    public static Specification<Store> greaterThanOrEqualFromDate(Long fromDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get(Store_.CREATED_AT), fromDate);
        };
    }

    public static Specification<Store> lessThanOrEqualToToDate(Long toDate) {
        return (root, query, criteriaBuilder) -> {
            if (toDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(Store_.CREATED_AT), toDate);
        };
    }


    public static Specification<Store> isStatus(String status) {
        List<String> statusList = List.of("A", "D");
        return (root, query, criteriaBuilder) -> {
            if (status == null || !statusList.contains(status)) return null;
            return criteriaBuilder.equal(root.get(Store_.STATUS), status);
        };
    }

    public static Specification<Store> hasSlug(String slug) {
        return (root, query, criteriaBuilder) -> {
            if (slug == null || slug.isEmpty()) return null;
            return criteriaBuilder.equal(root.get(Store_.SLUG), slug.trim());
        };
    }


}
