package com.sales.wholesaler.repository;


import com.sales.claims.AuthUser;
import com.sales.request.AddressRequest;
import com.sales.entities.City;
import com.sales.entities.State;
import com.sales.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
@RequiredArgsConstructor
public class WholesaleAddressHbRepository {

    private final EntityManager entityManager;

    public int updateAddress(AddressRequest addressRequest, AuthUser loggedUser){
        String hqQuery ="""
                UPDATE Address SET
                    city.id =:city,
                    state.id =:state,
                    latitude =:latitude,
                    altitude =:altitude,
                    updatedAt =:updatedAt,
                    updatedBy =:updatedBy
                WHERE id =:id 
            """;
        Query query = entityManager.createQuery(hqQuery);
        query.setParameter("city",addressRequest.getCity());
        query.setParameter("state",addressRequest.getState());
        query.setParameter("latitude",addressRequest.getLatitude());
        query.setParameter("altitude",addressRequest.getAltitude());
        query.setParameter("updatedBy", loggedUser.getId());
        query.setParameter("updatedAt", Utils.getCurrentMillis());
        query.setParameter("id",addressRequest.getAddressId());
        return  query.executeUpdate();
    }

    public List<City> getCityList(int stateId){
        String hql = "from City where stateId = :stateId";
        Query query = entityManager.createQuery(hql);
        query.setParameter("stateId", stateId);
        return query.getResultList();
    }

    public List<State> getStateList(){
        String hql = "from State";
        Query query = entityManager.createQuery(hql);
        return query.getResultList();
    }


}
