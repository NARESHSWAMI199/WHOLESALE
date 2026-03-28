package com.sales.specifications;

import com.sales.entities.Item;
import com.sales.entities.Item_;
import com.sales.global.USER_TYPES;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ItemsSpecifications {

    /**
     * there we create some predicates which is use for filters
     */


/*    public static Specification<Item> hasCategory(String category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null) return null;
            return criteriaBuilder.equal(root.get(Item_.ITEM_CATEGORY), category);
        };
    }*/
    public static Specification<Item> isWholesale(Integer wholesaleId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Item_.wholesaleId), wholesaleId);
    }


    public static Specification<Item> isWholesale(Integer wholesaleId, String userType) {
        return (root, query, criteriaBuilder) -> {
            if ((wholesaleId == null || wholesaleId == 0) && (userType.equals(USER_TYPES.STAFF.getType()) || userType.equals(USER_TYPES.SUPER_ADMIN.getType()))) {
                return null;
            } else if (wholesaleId == null || wholesaleId == 0) {
                throw new IllegalArgumentException("The store's id can't be null or 0.");
            } else {
                return criteriaBuilder.equal(root.get(Item_.wholesaleId), wholesaleId);
            }
        };
    }


    public static Specification<Item> containsName(String itemName) {
        return (root, query, criteriaBuilder) -> {
            if (itemName == null) return null;
            return criteriaBuilder.like(root.get(Item_.NAME), "%" + itemName + "%");
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
        List<String> labelList = List.of("N", "O");
        return (root, query, criteriaBuilder) -> {
            if (label == null || !labelList.contains(label)) return null;
            return criteriaBuilder.equal(root.get(Item_.LABEL), label);
        };
    }

    public static Specification<Item> inStock(String inStock) {
        List<String> stocks = List.of("Y", "N");
        return (root, query, criteriaBuilder) -> {
            if (inStock == null || !stocks.contains(inStock)) return null;
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
