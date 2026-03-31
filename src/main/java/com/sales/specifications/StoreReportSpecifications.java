package com.sales.specifications;

import com.sales.entities.StoreReport;
import com.sales.entities.StoreReport_;
import org.springframework.data.jpa.domain.Specification;

public class StoreReportSpecifications {


    public static Specification<StoreReport> hasStoreId(Integer storeId) {
        return (root, query, criteriaBuilder) -> {
            if (storeId == null || storeId == 0) return null;
            return criteriaBuilder.equal(root.get(StoreReport_.storeId), storeId);
        };
    }

}
