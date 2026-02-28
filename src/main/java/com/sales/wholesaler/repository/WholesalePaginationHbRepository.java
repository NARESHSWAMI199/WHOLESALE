package com.sales.wholesaler.repository;


import com.sales.claims.AuthUser;
import com.sales.request.UserPaginationRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor
public class WholesalePaginationHbRepository {

    private final EntityManager entityManager;

    public int updateUserPaginations(UserPaginationRequest userPaginationRequest, AuthUser loggedUser){
        String hql = """
                update UserPagination 
                set rowsNumber =:rowsNumber
                where userId = :userId and pagination.id = :paginationId
                """;
        Query query = entityManager.createQuery(hql);
        query.setParameter("rowsNumber", userPaginationRequest.getRowsNumber());
        query.setParameter("userId", loggedUser.getId());
        query.setParameter("paginationId", userPaginationRequest.getPaginationId());
        return query.executeUpdate();
    }

}
