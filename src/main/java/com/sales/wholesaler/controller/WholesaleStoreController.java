package com.sales.wholesaler.controller;


import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.request.SearchFilters;
import com.sales.request.StoreCreationRequest;
import com.sales.entities.Store;
import com.sales.global.ConstantResponseKeys;
import com.sales.jwtUtils.JwtToken;
import com.sales.utils.Utils;
import com.sales.wholesaler.dto.WholesaleCategoryDto;
import com.sales.wholesaler.dto.WholesaleStoreNotificationDto;
import com.sales.wholesaler.dto.WholesaleSubcategoryDto;
import com.sales.wholesaler.services.WholesaleStoreService;
import com.sales.wholesaler.services.WholesaleUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("wholesale/store")
@RequiredArgsConstructor
@Tag(name = "Wholesale Store Management", description = "APIs for managing stores for wholesalers")
public class WholesaleStoreController  {

    private final WholesaleStoreService wholesaleStoreService;
    private final JwtToken jwtToken;
    private final WholesaleUserService wholesaleUserService;
    private static final Logger logger = LoggerFactory.getLogger(WholesaleStoreController.class);

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema =
    @Schema(example = """
                {
                  "storeSlug" : "string",
                  "storeEmail": "string",
                  "storeName": "string",
                  "storePhone": "string",
                  "description": "string",
                  "storePic": "string",
                  "street": "string",
                  "zipCode": "string",
                  "city": 0,
                  "state": 0,
                  "categoryId" : "0",
                  "subcategoryId" : "0"
                }
                """
    )))
    @Transactional
    @PostMapping(value = {"/update"})
    @PreAuthorize("hasAnyAuthority('wholesale.store.udpate','wholesale.store.edit')")
    @Operation(summary = "Update store", description = "Updates store information for the authenticated wholesaler")
    public ResponseEntity<Map<String, Object>> updateStore(Authentication authentication,HttpServletRequest request, @ModelAttribute StoreCreationRequest storeCreationRequest) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateStore method");
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Map<String,Object> responseObj = wholesaleStoreService.updateStoreBySlug(storeCreationRequest, loggedUser);
        logger.debug("Completed updateStore method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @Transactional
    @PostMapping(value = {"notifications"})
    @PreAuthorize("hasAuthority('wholesale.store.notifications')")
    @Operation(summary = "Get store notifications", description = "Retrieves a paginated list of notifications for the wholesaler's store")
    public ResponseEntity<Page<WholesaleStoreNotificationDto>> getAllStoreNotification(Authentication authentication, HttpServletRequest request, @RequestBody SearchFilters searchFilters) {
        logger.debug("Starting getAllStoreNotification method");
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Page<WholesaleStoreNotificationDto> storeNotifications = wholesaleStoreService.getAllStoreNotification(searchFilters,loggedUser);
        logger.debug("Completed getAllStoreNotification method");
        return new ResponseEntity<>(storeNotifications, HttpStatus.OK);
    }



    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(
            example = """
                    "seenIds": [
                        "the list of seen notification ids"
                      ],
                    """
    )))
    @Transactional
    @PostMapping(value = {"update/notifications"})
    @PreAuthorize("hasAuthority('wholesale.store.notifications.seen')")
    @Operation(summary = "Update store notifications", description = "Marks specified notifications as seen for the wholesaler's store")
    public ResponseEntity<String> updateStoreNotification(@RequestBody StoreCreationRequest storeCreationRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateStoreNotification method");
        wholesaleStoreService.updateSeen(storeCreationRequest);
        logger.debug("Completed updateStoreNotification method");
        return new ResponseEntity<>(ConstantResponseKeys.SUCCESS, HttpStatus.valueOf(200));
    }


    @GetMapping("category")
    @Operation(summary = "Get all store categories", description = "Retrieves a list of all available store categories")
    public ResponseEntity<List<WholesaleCategoryDto>> getAllStoreCategory() {
        logger.debug("Starting getAllStoreCategory method");
        List<WholesaleCategoryDto> storeCategories = wholesaleStoreService.getAllStoreCategory();
        logger.debug("Completed getAllStoreCategory method");
        return new ResponseEntity<>(storeCategories, HttpStatus.OK);
    }


    @GetMapping("subcategory/{categoryId}")
    @Operation(summary = "Get store subcategories", description = "Retrieves all subcategories for a specific store category ID")
    public ResponseEntity<List<WholesaleSubcategoryDto>> getStoreSubCategory(@PathVariable(required = true) int categoryId) {
        logger.debug("Starting getStoreSubCategory method");
        List<WholesaleSubcategoryDto> storeSubCategories = wholesaleStoreService.getAllStoreSubCategories(categoryId);
        logger.debug("Completed getStoreSubCategory method");
        return new ResponseEntity<>(storeSubCategories, HttpStatus.OK);
    }



    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema =
        @Schema(example = """
                {
                  "storeEmail": "string",
                  "storeName": "string",
                  "storePhone": "string",
                  "description": "string",
                  "storePic": "string",
                  "street": "string",
                  "zipCode": "string",
                  "city": 0,
                  "state": 0,
                  "categoryId" : "0",
                  "subcategoryId" : "0"
                }
                """
    )))
    @PostMapping(value = "add",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasAnyAuthority('wholesale.store.add','wholesale.store.create')")
    @Transactional
    @Operation(summary = "Add new store", description = "Creates a new store for the authenticated wholesaler")
    public ResponseEntity<Map<String,Object>> addNewStore(HttpServletRequest request,@ModelAttribute StoreCreationRequest storeCreationRequest) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting addNewStore method");
        Map<String,Object> result = new HashMap<>();
        AuthUser loggedUser = (SalesUser) Utils.getUserFromRequest(request,jwtToken,wholesaleUserService);
        Store isInserted = wholesaleStoreService.createStore(storeCreationRequest,loggedUser);
        if(isInserted.getId() > 0){
            result.put(ConstantResponseKeys.MESSAGE,"Store created successfully. Welcome in Swami Sales");
            result.put(ConstantResponseKeys.STATUS,200);
        }else{
            result.put(ConstantResponseKeys.MESSAGE,"Something went wrong");
            result.put(ConstantResponseKeys.STATUS,400);
        }
        logger.debug("Completed addNewStore method");
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get("status")));
    }


}
