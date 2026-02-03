package com.sales.admin.services;

import com.sales.admin.repositories.AddressHbRepository;
import com.sales.admin.repositories.AddressRepository;
import com.sales.claims.AuthUser;
import com.sales.dto.AddressRequest;
import com.sales.entities.Address;
import com.sales.entities.City;
import com.sales.entities.State;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.sales.utils.Utils.getCurrentMillis;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressHbRepository addressHbRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);

    @Transactional
    public Address insertAddress(AddressRequest addressRequest, AuthUser loggedUser) {
        logger.debug("Entering insertAddress with addressRequest: {}, loggedUser: {}", addressRequest, loggedUser);
        Address address = new Address();
        address.setSlug(UUID.randomUUID().toString());

        address.setStreet(addressRequest.getStreet());
        address.setZipCode(addressRequest.getZipCode());

        address.setCity(City.builder()
                .id(addressRequest.getCity())
                .build()
        );
        address.setState(State.builder()
                .id(addressRequest.getState())
                .build()
        );

        address.setLatitude(addressRequest.getLatitude());
        address.setAltitude(addressRequest.getAltitude());

        address.setCreatedAt(getCurrentMillis());
        address.setCreatedBy(loggedUser.getId());
        address.setUpdatedAt(getCurrentMillis());
        address.setUpdatedBy(loggedUser.getId());
        Address result = addressRepository.save(address);
        logger.debug("Exiting insertAddress with result: {}", result);
        return result;
    }

    public List<City> getCityList(int stateId) {
        logger.debug("Entering getCityList with stateId: {}", stateId);
        List<City> result = addressHbRepository.getCityList(stateId);
        logger.debug("Exiting getCityList with result: {}", result);
        return result;
    }

    public List<State> getStateList() {
        logger.debug("Entering getStateList");
        List<State> result = addressHbRepository.getStateList();
        logger.debug("Exiting getStateList with result: {}", result);
        return result;
    }

}
