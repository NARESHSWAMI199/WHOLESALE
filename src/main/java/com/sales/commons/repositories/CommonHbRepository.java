package com.sales.commons.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface CommonHbRepository {

    default <T> Long getCounts(EntityManager entityManager, CriteriaBuilder cb, Specification<T> spec, Class<T> objectClass) {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(objectClass);
        cq.select(cb.count(root));
        if (spec != null) {
            cq.where(spec.toPredicate(root, cq, cb));
        }
        return entityManager.createQuery(cq).getSingleResult();
    }


    default void applySorting(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<?> root, Sort sort) {
        List<Order> orders = sort.stream()
                .map(o -> {
                    Path<?> path = root.get(o.getProperty());
                    return o.isAscending() ? cb.asc(path) : cb.desc(path);
                })
                .toList();
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        }
    }

}
