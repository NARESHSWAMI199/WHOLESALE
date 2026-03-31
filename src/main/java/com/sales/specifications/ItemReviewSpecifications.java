package com.sales.specifications;

import com.sales.claims.AuthUser;
import com.sales.entities.ItemReviews;
import com.sales.entities.ItemReviews_;
import com.sales.entities.StoreNotifications;
import com.sales.entities.StoreNotifications_;
import org.springframework.data.jpa.domain.Specification;

public class ItemReviewSpecifications {

    public static Specification<ItemReviews> containsName(String searchKey) {
        return (root, query, criteriaBuilder) -> {
            if (searchKey == null) return null;
            return criteriaBuilder.like(root.get(ItemReviews_.MESSAGE), "%" + searchKey + "%");
        };
    }


    public static Specification<ItemReviews> isItemId(Long itemId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(ItemReviews_.ITEM_ID), itemId);
    }

    public static Specification<ItemReviews> isParentComment(int parentId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(ItemReviews_.PARENT_ID), parentId);
    }


    public static Specification<ItemReviews> greaterThanOrEqualFromDate(Long fromDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get(ItemReviews_.CREATED_AT), fromDate);
        };
    }

    public static Specification<ItemReviews> lessThanOrEqualToToDate(Long toDate) {
        return (root, query, criteriaBuilder) -> {
            if (toDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(ItemReviews_.CREATED_AT), toDate);
        };
    }


    public static Specification<ItemReviews> hasSlug(String slug) {
        return (root, query, criteriaBuilder) -> {
            if (slug == null || slug.isEmpty()) return null;
            return criteriaBuilder.equal(root.get(ItemReviews_.SLUG), slug.trim());
        };
    }

    public static Specification<ItemReviews> notSuperAdmin(AuthUser loggedUser) {
        return (root, query, criteriaBuilder) -> {
            if (loggedUser.getId() == 0) return null;
            return criteriaBuilder.notEqual(root.get(ItemReviews_.ID), 0);
        };
    }

    public static Specification<StoreNotifications> isUserId(int userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == 0) return null;
            return criteriaBuilder.equal(root.get(StoreNotifications_.USER_ID), userId);
        };
    }


    public static Specification<StoreNotifications> isWholesaleId(int isWholesaleId) {
        return (root, query, criteriaBuilder) -> {
            if (isWholesaleId == 0) return null;
            return criteriaBuilder.equal(root.get(StoreNotifications_.WHOLESALE_ID), isWholesaleId);
        };
    }
}
