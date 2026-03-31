package com.sales.specifications;

import com.sales.entities.Pagination;
import com.sales.entities.Pagination_;
import org.springframework.data.jpa.domain.Specification;

public class PaginationSpecification {

    public static Specification<Pagination> whoCanSee(String canSee) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Pagination_.CAN_SEE), canSee);
    }
}
