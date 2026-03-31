package com.sales.specifications;

import com.sales.entities.ServicePlan;
import com.sales.entities.ServicePlan_;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ServicePlanSpecification {

    public static Specification<ServicePlan> containsName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null) return null;
            return criteriaBuilder.like(root.get(ServicePlan_.NAME), "%" + name + "%");
        };
    }

    public static Specification<ServicePlan> greaterThanOrEqualFromDate(Long fromDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get(ServicePlan_.CREATED_AT), fromDate);
        };
    }

    public static Specification<ServicePlan> lessThanOrEqualToToDate(Long toDate) {
        return (root, query, criteriaBuilder) -> {
            if (toDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(ServicePlan_.CREATED_AT), toDate);
        };
    }


    public static Specification<ServicePlan> isStatus(String status) {
        List<String> statusList = List.of("A", "D");
        return (root, query, criteriaBuilder) -> {
            if (status == null || !statusList.contains(status)) return null;
            return criteriaBuilder.equal(root.get(ServicePlan_.STATUS), status);
        };
    }


    public static Specification<ServicePlan> equalOrLessThanPrice(Float price) {
        return (root, query, criteriaBuilder) -> {
            if (price == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(ServicePlan_.PRICE), price);
        };
    }


    public static Specification<ServicePlan> hasSlug(String slug) {
        return (root, query, criteriaBuilder) -> {
            if (slug == null || slug.trim().equals("")) return null;
            return criteriaBuilder.equal(root.get(ServicePlan_.SLUG), slug.trim());
        };
    }

}
