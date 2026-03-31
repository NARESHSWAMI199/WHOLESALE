package com.sales.specifications;

import com.sales.claims.AuthUser;
import com.sales.entities.Group;
import com.sales.entities.Group_;
import org.springframework.data.jpa.domain.Specification;

public class GroupSpecifications {


    public static Specification<Group> isGroupId(Integer groupId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Group_.ID), groupId);
    }


    public static Specification<Group> containsName(String searchKey) {
        return (root, query, criteriaBuilder) -> {
            if (searchKey == null) return null;
            return criteriaBuilder.like(root.get(Group_.NAME), "%" + searchKey + "%");
        };
    }


    public static Specification<Group> greaterThanOrEqualFromDate(Long fromDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get(Group_.CREATED_AT), fromDate);
        };
    }

    public static Specification<Group> lessThanOrEqualToToDate(Long toDate) {
        return (root, query, criteriaBuilder) -> {
            if (toDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(Group_.CREATED_AT), toDate);
        };
    }


    public static Specification<Group> hasSlug(String slug) {
        return (root, query, criteriaBuilder) -> {
            if (slug == null || slug.isEmpty()) return null;
            return criteriaBuilder.equal(root.get(Group_.SLUG), slug.trim());
        };
    }

    public static Specification<Group> notSuperAdmin(AuthUser loggedUser) {
        return (root, query, criteriaBuilder) -> {
            if (loggedUser.getUserType().equals("SA")) return null;
            return criteriaBuilder.notEqual(root.get(Group_.ID), 0);
        };
    }
}
