package com.sales.wholesaler.controller;


import com.sales.admin.services.AddressService;
import com.sales.entities.City;
import com.sales.entities.State;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wholesale/address")
@RequiredArgsConstructor
@Tag(name = "Wholesale Address Management", description = "APIs for managing addresses for wholesalers")
public class WholesaleAddressController  {

    private final AddressService addressService;
    private static final Logger logger = LoggerFactory.getLogger(WholesaleAddressController.class);

    @GetMapping("/city/{stateId}")
    @Operation(summary = "Get cities by state", description = "Retrieves the list of cities for a given state ID")
    public ResponseEntity<List<City>> getCityList(@PathVariable Integer stateId ) {
        logger.debug("Starting getCityList method");
        List<City> cityList = addressService.getCityList(stateId);
        logger.debug("Completed getCityList method");
        return new ResponseEntity<>(cityList, HttpStatus.OK);
    }

    @GetMapping("/state")
    @Operation(summary = "Get all states", description = "Retrieves the list of all states")
    public ResponseEntity<List<State>> getStateList() {
        logger.debug("Starting getStateList method");
        List<State> stateList = addressService.getStateList();
        logger.debug("Completed getStateList method");
        return new ResponseEntity<>(stateList,HttpStatus.OK);
    }


}
