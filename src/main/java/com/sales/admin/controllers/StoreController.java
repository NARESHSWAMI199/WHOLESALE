package com.sales.admin.controllers;

import com.sales.admin.services.StoreService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.*;
import com.sales.entities.Store;
import com.sales.entities.StoreCategory;
import com.sales.entities.StoreSubCategory;
import com.sales.exceptions.MyException;
import com.sales.global.ConstantResponseKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("admin/store")
@RequiredArgsConstructor
@Tag(name = "Store Management", description = "APIs for managing stores, categories, and related operations")
public class StoreController {

    private final StoreService storeService;
    
    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

    @PostMapping("/all")
    @PreAuthorize("hasAuthority('store.all')")
    @Operation(summary = "Get all stores", description = "Retrieves a paginated list of all stores with optional search filters")
    public ResponseEntity<Page<Store>> getAllStore(@RequestBody SearchFilters searchFilters){
        logger.debug("Fetching all stores with filters: {}", searchFilters);
        Page<Store> storePage =  storeService.getAllStore(searchFilters);
        return new ResponseEntity<>(storePage, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('store.delete')")
    @Operation(summary = "Delete store", description = "Deletes a store by its slug")
    public ResponseEntity<Map<String,Object>> deleteStore(Authentication authentication,HttpServletRequest request, @RequestBody DeleteDto deleteDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Deleting store with slug: {}", deleteDto.getSlug());
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isUpdated = storeService.deleteStoreBySlug(deleteDto,loggedUser);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Store has been successfully deleted.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        }else{
            responseObj.put(ConstantResponseKeys.MESSAGE, "No store found to delete");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj,HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }



    @GetMapping("/detail/{slug}")
    @PreAuthorize("hasAuthority('store.detail')")
    @Operation(summary = "Get store details", description = "Retrieves detailed information about a store by its slug")
    public ResponseEntity<Map<String,Object>> getStoreDetailBySlug(@PathVariable String slug) {
        logger.debug("Fetching store details for params: {}", slug);
        Map<String,Object> responseObj = new HashMap<>();
        Store store = storeService.getStoreDetails(slug);
        if (store!= null){
            responseObj.put(ConstantResponseKeys.RES, store);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        }else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No store found.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj,HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @GetMapping("/detailbyuser/{userSLug}")
    @PreAuthorize("hasAuthority('store.user')")
    @Operation(summary = "Get store details by user slug", description = "Retrieves store details associated with a user by user slug")
    public ResponseEntity<Map<String,Object>> getUserDetailByUserSlug(@PathVariable("userSLug") String slug) {
        logger.debug("Fetching store details for user slug: {}", slug);
        Map<String,Object> responseObj = new HashMap<>();
        Store store = storeService.getStoreByUserSlug(slug);
        if (store!= null){
            responseObj.put(ConstantResponseKeys.RES, store);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        }else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No record found.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj,HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }




    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            schema = @Schema(example = """
                {
                    "storeSlug" : "string (during update only)",
                    "userSlug" : "string (during create only)",
                    "storeName" : "string",
                    "storeEmail" : "string",
                    "description" : "string",
                    "categoryId" : 0,
                    "subCategoryId"  : 0,
                    "storePhone" : "string",
                    "zipCode" : "string(6 digit)",
                    "city" : "cityId",
                    "state" : "stateId",
                    "street" : "string"
                }
                """)
    ))
    @Transactional
    @PostMapping(value = {"/add","/update"})
    @PreAuthorize("hasAnyAuthority('store.add','store.update','store.edit')")
    @Operation(summary = "Add or update store", description = "Creates a new store or updates an existing store based on the provided data")
    public ResponseEntity<Map<String,Object>> addStoreOrUpdateStore(Authentication authentication,HttpServletRequest request,  @ModelAttribute StoreDto storeDto) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Adding or updating store with details: {}", storeDto);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        String path = request.getRequestURI().toLowerCase();
        Map<String,Object> responseObj = storeService.createOrUpdateStore(storeDto,loggedUser,path);
        return new ResponseEntity<>(responseObj,HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    /** currently we do not use it for upload store image */
    @Transactional
    @PostMapping("profile/{slug}")
    @PreAuthorize("hasAnyAuthority('store.profile.update','store.profile.edit')")
    @Operation(summary = "Upload store image", description = "Uploads and updates the profile image for a store")
    public ResponseEntity<Map<String,Object>> uploadStoreImage(HttpServletRequest request, @RequestPart MultipartFile storeImage , @PathVariable String slug) {
        logger.debug("Uploading store image for slug: {}",slug);
        Map<String,Object> responseObj = new HashMap<>();
        try {
            int isUpdated = storeService.updateStoreImage(storeImage, slug);
            if (isUpdated > 0){
                responseObj.put(ConstantResponseKeys.MESSAGE,"successfully updated");
                responseObj.put(ConstantResponseKeys.STATUS,200);
            }else{
                responseObj.put(ConstantResponseKeys.MESSAGE,"No record found.");
                responseObj.put(ConstantResponseKeys.STATUS,404);
            }

        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObj.put(ConstantResponseKeys.MESSAGE,e.getMessage());
            responseObj.put(ConstantResponseKeys.STATUS,500);
        }
        return new ResponseEntity<>(responseObj,HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));


    }



    @Transactional
    @PostMapping("/status")
    @PreAuthorize("hasAuthority('store.status')")
    @Operation(summary = "Update store status", description = "Updates the status of a store (active/inactive)")
    public ResponseEntity<Map<String,Object>> updateStoreStatus (@RequestBody StatusDto statusDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Updating store status for slug: {}", statusDto.getSlug());
        Map<String,Object> responseObj = new HashMap<>();
        int isUpdated = storeService.updateStatusBySlug(statusDto);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Store's status has been successfully updated.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        }else{
            responseObj.put(ConstantResponseKeys.MESSAGE, "No store found.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj,HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }



    @Value("${store.get}")
    String filePath;

    @GetMapping("/image/{slug}/{filename}")
    @Operation(summary = "Get store image", description = "Retrieves the profile image of a store")
    public ResponseEntity<Resource> getFile(@PathVariable(required = true) String filename , @PathVariable String slug) throws Exception {
        logger.debug("Fetching store image: {} for slug: {}",filename, slug);
        Path filePathObj = Paths.get(filePath);
        Path userSlug = filePathObj.resolve(slug).normalize();
        Path path = userSlug.resolve(filename).normalize();
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }


    @Transactional(rollbackOn = {MyException.class ,RuntimeException.class})
    @PostMapping("category")
    @Operation(summary = "Get all store categories", description = "Retrieves a list of all store categories with optional filters")
    public ResponseEntity<List<StoreCategory>> getAllStoreCategory(@RequestBody SearchFilters searchFilters) {
        logger.debug("Fetching all store categories with filters: {}", searchFilters);
        List<StoreCategory> storeCategories = storeService.getAllStoreCategory(searchFilters);
        return new ResponseEntity<>(storeCategories, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('store.category.add','store.category.update','store.category.edit')")
    @PostMapping(value = {"category/add","category/update"})
    @Operation(summary = "Add or update store category", description = "Creates a new store category or updates an existing one")
    public ResponseEntity<Map<String,Object>> saveOrUpdateItemCategory(@RequestBody CategoryDto categoryDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Saving or updating store category with details: {}", categoryDto);
        Map<String,Object> result = new HashMap<>();
        StoreCategory updatedStoreCategory = storeService.saveOrUpdateStoreCategory(categoryDto);
        if(updatedStoreCategory != null) {
             result.put(ConstantResponseKeys.RES,updatedStoreCategory);
            if(categoryDto.getId() !=null && categoryDto.getId() != 0) {
                result.put(ConstantResponseKeys.MESSAGE, "Category successfully updated.");
                result.put(ConstantResponseKeys.STATUS, 200);
            }else {
                result.put(ConstantResponseKeys.MESSAGE, "Category successfully inserted.");
                result.put(ConstantResponseKeys.STATUS, 201);
            }
        }
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get("status")));
    }

    @PreAuthorize("hasAnyAuthority('store.category.detail')")
    @GetMapping("category/{categoryId}")
    @Operation(summary = "Get store category details", description = "Retrieves details of a specific store category by ID")
    public ResponseEntity<StoreCategory> getAllCategory(@PathVariable Integer categoryId) {
        logger.debug("Fetching store category with ID: {}", categoryId);
        StoreCategory storeCategory = storeService.getStoreCategoryById(categoryId);
        return new ResponseEntity<>(storeCategory, HttpStatus.OK);
    }


    @PostMapping("category/delete")
    @PreAuthorize("hasAuthority('store.category.delete')")
    @Operation(summary = "Delete store category", description = "Deletes a store category by its slug")
    public ResponseEntity<Map<String,Object>> deleteItemCategoryById(Authentication authentication,HttpServletRequest request ,@RequestBody DeleteDto deleteDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Deleting store category with slug: {}", deleteDto.getSlug());
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser user = (SalesUser) authentication.getPrincipal();
        int isUpdated = storeService.deleteStoreCategory(deleteDto,user);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Store's category was successfully deleted.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No category found to delete.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @Transactional(rollbackOn = {MyException.class ,RuntimeException.class})
    @PostMapping("subcategory")
    @Operation(summary = "Get all store subcategories", description = "Retrieves a list of all store subcategories with optional filters")
    public ResponseEntity<List<StoreSubCategory>> getStoreSubCategory(@RequestBody SearchFilters searchFilters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Fetching all store subcategories with filters: {}", searchFilters);
        List<StoreSubCategory> storeSubCategories = storeService.getAllStoreSubCategories(searchFilters);
        return new ResponseEntity<>(storeSubCategories, HttpStatus.OK);
    }


    @PostMapping("subcategory/delete")
    @PreAuthorize("hasAuthority('store.subcategory.delete')")
    @Operation(summary = "Delete store subcategory", description = "Deletes a store subcategory by its slug")
    public ResponseEntity<Map<String,Object>> deleteItemSubCategoryById(Authentication authentication,HttpServletRequest request,@RequestBody DeleteDto deleteDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Deleting store subcategory with slug: {}", deleteDto.getSlug());
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser user = (SalesUser) authentication.getPrincipal();
        int isUpdated = storeService.deleteStoreSubCategory(deleteDto,user);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Store's subcategory successfully deleted.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No subcategory found to delete.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @PostMapping(value = {"subcategory/add","subcategory/update"})
    @PreAuthorize("hasAnyAuthority('store.subcategory.add','store.subcategory.update','store.subcategory.edit')")
    public ResponseEntity<Map<String,Object>> saveOrUpdateItemSubCategory(@RequestBody SubCategoryDto subCategoryDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Saving or updating store subcategory with details: {}", subCategoryDto);
        Map<String,Object> result = new HashMap<>();
        StoreSubCategory updatedStoreSubCategory = storeService.saveOrUpdateStoreSubCategory(subCategoryDto);
        if(updatedStoreSubCategory != null) {
            result.put(ConstantResponseKeys.RES,updatedStoreSubCategory);
            if(subCategoryDto.getId() != null) {
                result.put(ConstantResponseKeys.MESSAGE, "Subcategory successfully updated.");
                result.put(ConstantResponseKeys.STATUS, 200);
            }else {
                result.put(ConstantResponseKeys.MESSAGE, "Subcategory successfully inserted.");
                result.put(ConstantResponseKeys.STATUS, 201);
            }
        }
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get("status")));
    }



}
