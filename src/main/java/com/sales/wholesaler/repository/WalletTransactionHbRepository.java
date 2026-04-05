package com.sales.wholesaler.repository;


import com.sales.commons.repositories.CommonHbRepository;
import com.sales.entities.WalletTransaction;
import com.sales.entities.WalletTransaction_;
import com.sales.wholesaler.dto.WholesaleWalletTransactionDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WalletTransactionHbRepository implements CommonHbRepository {

    private final EntityManager entityManager;

    public Page<WholesaleWalletTransactionDto> findAll(Specification<WalletTransaction> spec, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        Long itemsCounts = getCounts(entityManager, criteriaBuilder, spec, WalletTransaction.class);
        if (itemsCounts == 0) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }
        List<WholesaleWalletTransactionDto> content = getAllFilteredWalletTransactions(criteriaBuilder, spec, pageable);
        return new PageImpl<>(content, pageable, itemsCounts);
    }

    public List<WholesaleWalletTransactionDto> getAllFilteredWalletTransactions(CriteriaBuilder criteriaBuilder, Specification<WalletTransaction> spec, Pageable pageable) {
        CriteriaQuery<WholesaleWalletTransactionDto> criteriaQuery = criteriaBuilder.createQuery(WholesaleWalletTransactionDto.class);
        Root<WalletTransaction> root = criteriaQuery.from(WalletTransaction.class);
        criteriaQuery.multiselect(
                root.get(WalletTransaction_.slug),
                root.get(WalletTransaction_.userId),
                root.get(WalletTransaction_.amount),
                root.get(WalletTransaction_.createdAt),
                root.get(WalletTransaction_.transactionType),
                root.get(WalletTransaction_.status)
        );

        if (spec != null) {
            criteriaQuery.where(spec.toPredicate(root, criteriaQuery, criteriaBuilder));
        }
        // Sorting
        applySorting(criteriaBuilder, criteriaQuery, root, pageable.getSort());
        TypedQuery<WholesaleWalletTransactionDto> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return query.getResultList();
    }

}
