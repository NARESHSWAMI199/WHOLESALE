package com.sales.wholesaler.repository;


import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor
public class WholesaleUserPlanHbRepository {

    private final EntityManager entityManager;

}
