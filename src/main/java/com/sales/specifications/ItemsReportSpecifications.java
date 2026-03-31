package com.sales.specifications;

import com.sales.entities.Item;
import com.sales.entities.ItemReport;
import com.sales.entities.ItemReport_;
import com.sales.entities.Item_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ItemsReportSpecifications {

    /**
     * there we create some predicates which is use for filters
     */


/*    public static Specification<Item> hasCategory(String category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null) return null;
            return criteriaBuilder.equal(root.get(Item_.ITEM_CATEGORY), category);
        };
    }*/
    public static Specification<ItemReport> containsMessage(String itemName) {
        return (root, query, criteriaBuilder) -> {
            if (itemName == null) return null;
            return criteriaBuilder.like(root.get(Item_.NAME), "%" + itemName + "%");
        };
    }


    public static Specification<ItemReport> hasItemId(Long itemId) {
        return (root, query, criteriaBuilder) -> {
            if (itemId == null || itemId == 0) return null;
            return criteriaBuilder.equal(root.get(ItemReport_.ITEM_ID), itemId);
        };
    }


    public static Specification<Item> greaterThanOrEqualFromDate(Long fromDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get(Item_.CREATED_AT), fromDate);
        };
    }

    public static Specification<Item> lessThanOrEqualToToDate(Long toDate) {
        return (root, query, criteriaBuilder) -> {
            if (toDate == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(Item_.CREATED_AT), toDate);
        };
    }


    public static Specification<Item> isStatus(String status) {
        List<String> statusList = List.of("A", "D");
        return (root, query, criteriaBuilder) -> {
            if (status == null || !statusList.contains(status)) return null;
            return criteriaBuilder.equal(root.get(Item_.STATUS), status);
        };
    }


    public static Specification<Item> isLabel(String label) {
        return (root, query, criteriaBuilder) -> {
            if (label == null) return null;
            return criteriaBuilder.equal(root.get(Item_.LABEL), label);
        };
    }

    public static Specification<Item> inStock(String inStock) {
        return (root, query, criteriaBuilder) -> {
            if (inStock == null) return null;
            return criteriaBuilder.equal(root.get(Item_.IN_STOCK), inStock);
        };
    }

    public static Specification<Item> equalOrLessThanPrice(Float price) {
        return (root, query, criteriaBuilder) -> {
            if (price == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get(Item_.price), price);
        };
    }


    public static Specification<Item> hasSlug(String slug) {
        return (root, query, criteriaBuilder) -> {
            if (slug == null || slug.trim().equals("")) return null;
            return criteriaBuilder.equal(root.get(Item_.slug), slug.trim());
        };
    }


}
