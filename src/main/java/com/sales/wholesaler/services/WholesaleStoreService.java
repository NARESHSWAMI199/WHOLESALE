package com.sales.wholesaler.services;

import com.sales.admin.repositories.AddressRepository;
import com.sales.claims.AuthUser;
import com.sales.entities.*;
import com.sales.exceptions.MyException;
import com.sales.exceptions.NotFoundException;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import com.sales.global.ResponseMessages;
import com.sales.request.AddressRequest;
import com.sales.request.SearchFilters;
import com.sales.request.StoreCreationRequest;
import com.sales.utils.UploadImageValidator;
import com.sales.utils.Utils;
import com.sales.wholesaler.dto.WholesaleCategoryDto;
import com.sales.wholesaler.dto.WholesaleStoreDto;
import com.sales.wholesaler.dto.WholesaleStoreNotificationDto;
import com.sales.wholesaler.dto.WholesaleSubcategoryDto;
import com.sales.wholesaler.mapper.WholesaleCategoryMapper;
import com.sales.wholesaler.mapper.WholesaleStoreMapper;
import com.sales.wholesaler.mapper.WholesaleStoreNotificationMapper;
import com.sales.wholesaler.mapper.WholesaleSubcategoryMapper;
import com.sales.wholesaler.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.sales.helpers.PaginationHelper.getPageable;
import static com.sales.specifications.ItemReviewSpecifications.isUserId;
import static com.sales.specifications.ItemReviewSpecifications.isWholesaleId;
import static com.sales.utils.Utils.getCurrentMillis;

@Service
@RequiredArgsConstructor
public class WholesaleStoreService {

    private static final Logger logger = LoggerFactory.getLogger(WholesaleStoreService.class);
    private final WholesaleCategoryRepository wholesaleCategoryRepository;
    private final WholesaleSubCategoryRepository wholesaleSubCategoryRepository;
    private final WholesaleAddressHbRepository wholesaleAddressHbRepository;
    private final WholesaleStoreHbRepository wholesaleStoreHbRepository;
    private final WholesaleStoreRepository wholesaleStoreRepository;
    private final AddressRepository addressRepository;
    private final WholesaleNotificationRepository wholesaleNotificationRepository;
    private final WholesaleStoreMapper wholesaleStoreMapper;
    private final WholesaleStoreNotificationMapper wholesaleStoreNotificationMapper;
    private final WholesaleCategoryMapper wholesaleCategoryMapper;
    private final WholesaleSubcategoryMapper wholesaleSubCategoryMapper;
    private final WholesalePermissionHbRepository permissionHbRepository;
    private final WholsaleStorePermissionsRepository storePermissionsRepository;
    @Value("${store.absolute}")
    String storeImagePath;

    @Transactional(rollbackFor = {IllegalArgumentException.class, MyException.class, RuntimeException.class})
    public Map<String, Object> updateStoreBySlug(StoreCreationRequest storeCreationRequest, AuthUser loggedUser) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateStoreBySlug method with storeCreationRequest: {}, loggedUser: {}", storeCreationRequest, loggedUser);

        // Validating required fields. If their we found any required field is null, this will throw an Exception
        Utils.checkRequiredFields(storeCreationRequest, List.of("storeName", "storeEmail", "storePhone", "categoryId", "subCategoryId"));

        Map<String, Object> responseObj = new HashMap<>();
        String storeName = Utils.isValidName(storeCreationRequest.getStoreName(), ConstantResponseKeys.STORE);
        storeCreationRequest.setStoreName(storeName);

        /* '_' replaced by actual error message in mobileAndEmailValidation */
        Utils.mobileAndEmailValidation(storeCreationRequest.getStoreEmail(), storeCreationRequest.getStorePhone(), "Not a valid _");

        try {
            StoreCategory storeCategory = wholesaleCategoryRepository.findById(storeCreationRequest.getCategoryId()).orElseThrow(() -> new NotFoundException("Store category not found."));
            storeCreationRequest.setStoreCategory(storeCategory);
            StoreSubCategory storeSubCategory = wholesaleSubCategoryRepository.findById(storeCreationRequest.getSubCategoryId()).orElseThrow(() -> new NotFoundException("Store subcategory not found."));
            storeCreationRequest.setStoreSubCategory(storeSubCategory);
        } catch (Exception e) {
            throw new MyException(ResponseMessages.INVALID_ARGUMENTS_FOR_CATEGORY_AND_SUBCATEGORY);
        }
        Store store = getStoreByUserId(loggedUser.getId());
        String slug = store.getSlug();
        storeCreationRequest.setStoreSlug(slug);

        // before update store and store's address get address id from store
        Integer addressId = wholesaleStoreRepository.getAddressIdBySlug(storeCreationRequest.getStoreSlug());
        if (addressId == null)
            throw new IllegalArgumentException(ResponseMessages.NO_STORE_FOUND_TO_UPDATE);  // wrong wholesale slug.
        storeCreationRequest.setAddressId(addressId);

        String imageName = getStoreImagePath(storeCreationRequest.getStorePic(), slug);
        if (imageName != null) {
            storeCreationRequest.setStoreAvatar(imageName);
        } else {
            storeCreationRequest.setStoreAvatar(store.getAvtar());
        }
        int isUpdated = updateStore(storeCreationRequest, loggedUser); // Update operation
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SUCCESSFULLY_UPDATED);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.NO_STORE_FOUND_TO_UPDATE);
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        logger.debug("Completed updateStoreBySlug method");
        return responseObj;
    }

    public int updateStore(StoreCreationRequest storeCreationRequest, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateStore method with storeCreationRequest: {}, loggedUser: {}", storeCreationRequest, loggedUser);
        AddressRequest address = new AddressRequest();
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(storeCreationRequest, List.of("street", "zipCode", "city", "state"));
        address.setStreet(storeCreationRequest.getStreet());
        address.setZipCode(storeCreationRequest.getZipCode());
        address.setCity(storeCreationRequest.getCity());
        address.setState(storeCreationRequest.getState());
        address.setAddressId(storeCreationRequest.getAddressId());
        int isUpdatedAddress = wholesaleAddressHbRepository.updateAddress(address, loggedUser); // Update operation
        if (isUpdatedAddress < 1) return isUpdatedAddress;
        int isUpdatedStore = wholesaleStoreHbRepository.updateStore(storeCreationRequest, loggedUser); // Update operation
        logger.debug("Completed updateStore method");
        return isUpdatedStore;
    }


    @Transactional
    public WholesaleStoreDto getStoreDtoByUserSlug(String slug) {
        logger.debug("Starting getStoreByUserSlug method with slug: {}", slug);
        Store store = wholesaleStoreRepository.getStoreByUserSlug(slug);
        logger.debug("Completed getStoreByUserSlug method");
        return wholesaleStoreMapper.toDto(store);
    }


    public Store getStoreByUserId(Integer userId) {
        logger.debug("Starting getStoreByUserId method with userId: {}", userId);
        Store store = wholesaleStoreRepository.findStoreByUserId(userId);
        logger.debug("Completed getStoreByUserId method");
        return store;
    }


    @Transactional
    public WholesaleStoreDto getStoreDtoByUserId(Integer userId) {
        logger.debug("Starting getStoreDtoByUserId method with userId: {}", userId);
        Store store = wholesaleStoreRepository.findStoreByUserId(userId);
        logger.debug("Completed getStoreDtoByUserId method");
        return wholesaleStoreMapper.toDto(store);
    }

    public Integer getStoreIdByUserSlug(Integer userId) {
        logger.debug("Starting getStoreIdByUserSlug method with userId: {}", userId);
        Integer storeId = wholesaleStoreRepository.getStoreIdByUserId(userId);
        logger.debug("Completed getStoreIdByUserSlug method");
        return storeId;
    }

    public String getStoreImagePath(MultipartFile storeImage, String slug) throws MyException, IOException {
        logger.debug("Starting getStoreImagePath method with storeImage: {}, slug: {}", storeImage, slug);
        if (storeImage != null) {
            if (UploadImageValidator.isValidImage(storeImage, GlobalConstant.bannerMinWidth,
                    GlobalConstant.bannerMinHeight, GlobalConstant.bannerMaxWidth, GlobalConstant.bannerMaxHeight,
                    GlobalConstant.allowedAspectRatios, GlobalConstant.allowedFormats)) {
                String fileOriginalName = Objects.requireNonNull(storeImage.getOriginalFilename()).replaceAll(" ", "_");
                String dirPath = storeImagePath + slug + GlobalConstant.PATH_SEPARATOR;
                File dir = new File(dirPath);
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dirPath + fileOriginalName);
                storeImage.transferTo(file);
                logger.debug("Completed getStoreImagePath method");
                return fileOriginalName;
            } else {
                throw new MyException(ResponseMessages.IMAGE_IS_NOT_FIT_IN_ACCEPT_RATIO_PLEASE_RESIZE_YOUR_IMAGE_BEFORE_UPLOAD);
            }
        }
        return null;
    }

    @Transactional(rollbackFor = {MyException.class, IllegalArgumentException.class, RuntimeException.class, Exception.class})
    public Store createStore(StoreCreationRequest storeCreationRequest, AuthUser loggedUser) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting createStore method with storeCreationRequest: {}, loggedUser: {}", storeCreationRequest, loggedUser);

        // Validating required fields. If their we found any required field is null, this will throw an Exception
        Utils.checkRequiredFields(storeCreationRequest, List.of("storeName", "storePic", "storeEmail", "storePhone", "categoryId", "subCategoryId"));

        /* '_' replaced by actual error message in mobileAndEmailValidation */
        Utils.mobileAndEmailValidation(storeCreationRequest.getStoreEmail(), storeCreationRequest.getStorePhone(), "Not a valid _");

        try {
            StoreCategory storeCategory = wholesaleCategoryRepository.findById(storeCreationRequest.getCategoryId()).orElseThrow(() -> new NotFoundException("Store category not found."));
            storeCreationRequest.setStoreCategory(storeCategory);
            StoreSubCategory storeSubCategory = wholesaleSubCategoryRepository.findById(storeCreationRequest.getSubCategoryId()).orElseThrow(() -> new NotFoundException("Store subcategory not found."));
            storeCreationRequest.setStoreSubCategory(storeSubCategory);
        } catch (Exception e) {
            throw new MyException(ResponseMessages.INVALID_ARGUMENTS_FOR_CATEGORY_AND_SUBCATEGORY);
        }

        /* inserting  address during create a wholesale */
        AddressRequest addressRequest = getAddressObjFromStore(storeCreationRequest);
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(addressRequest, List.of("street", "zipCode", "city", "state"));
        Address address = insertAddress(addressRequest, loggedUser); // Create operation

        Store store = new Store(loggedUser);
        store.setUser(User.builder().id(loggedUser.getId()).build());
        store.setStoreName(storeCreationRequest.getStoreName());
        store.setEmail(storeCreationRequest.getStoreEmail());
        store.setAddress(address);
        store.setDescription(storeCreationRequest.getDescription());
        store.setPhone(storeCreationRequest.getStorePhone());
        store.setRating(0f);
        store.setStoreCategory(storeCreationRequest.getStoreCategory());
        store.setStoreSubCategory(storeCreationRequest.getStoreSubCategory());
        Store insertedStore = wholesaleStoreRepository.save(store); // Create operation
        String imageName = getStoreImagePath(storeCreationRequest.getStorePic(), insertedStore.getSlug());
        if (imageName != null) {
            store.setAvtar(imageName); /** I know save function called before set this, but it will save automatically due to same transaction */
        } else {
            throw new MyException(ResponseMessages.STORE_IMAGE_CAN_T_BE_BLANK);
        }
        logger.debug("Completed createStore method");

        // Providing default permissions to wholesaler
        List<Integer> defaultPermissions = storePermissionsRepository.getAllDefaultPermissionsIds();
        int isAssigned = permissionHbRepository.assignPermissionsToWholesaler(loggedUser.getId(), defaultPermissions);
        if (isAssigned < 1)
            throw new MyException(ResponseMessages.SOMETHING_WENT_WRONG_DURING_UPDATE_WHOLESALER_S_PERMISSIONS_PLEASE_CONTACT_TO_ADMINISTRATOR);
        return insertedStore;
    }

    @Transactional
    public Address insertAddress(AddressRequest addressRequest, AuthUser loggedUser) {
        logger.debug("Starting insertAddress method with addressRequest: {}, loggedUser: {}", addressRequest, loggedUser);
        Address address = Address.builder()
                .slug(UUID.randomUUID().toString())
                .street(addressRequest.getStreet())
                .zipCode(addressRequest.getZipCode())
                .city(City.builder()
                        .id(addressRequest.getCity())
                        .build()
                )
                .state(State.builder()
                        .id(addressRequest.getState())
                        .build()
                )
                .latitude(addressRequest.getLatitude())
                .altitude(addressRequest.getAltitude())
                .createdAt(getCurrentMillis())
                .createdBy(loggedUser.getId())
                .updatedAt(getCurrentMillis())
                .updatedBy(loggedUser.getId())
                .build();
        Address savedAddress = addressRepository.save(address); // Create operation
        logger.debug("Completed insertAddress method");
        return savedAddress;
    }

    public AddressRequest getAddressObjFromStore(StoreCreationRequest storeCreationRequest) {
        logger.debug("Starting getAddressObjFromStore method with storeCreationRequest: {}", storeCreationRequest);
        AddressRequest addressRequest = AddressRequest.builder()
                .street(storeCreationRequest.getStreet())
                .zipCode(storeCreationRequest.getZipCode())
                .city(storeCreationRequest.getCity())
                .state(storeCreationRequest.getState())
                .latitude(storeCreationRequest.getLatitude())
                .altitude(storeCreationRequest.getAltitude())
                .build();
        logger.debug("Completed getAddressObjFromStore method");
        return addressRequest;
    }

    @Transactional
    public Page<WholesaleStoreNotificationDto> getAllStoreNotification(SearchFilters filters, AuthUser loggedUser) {
        logger.debug("Starting getAllStoreNotification method with filters: {}, loggedUser: {}", filters, loggedUser);
        Integer storeId = wholesaleStoreRepository.getStoreIdByUserId(loggedUser.getId());
        Specification<StoreNotifications> specification = Specification.allOf(isUserId(loggedUser.getId()).or(isWholesaleId(storeId)));
        Pageable pageable = getPageable(logger, filters);
        Page<StoreNotifications> notificationsPage = wholesaleNotificationRepository.findAll(specification, pageable);
        logger.debug("Completed getAllStoreNotification method");
        return notificationsPage.map(wholesaleStoreNotificationMapper::toDto);
    }

    public void updateSeen(StoreCreationRequest storeCreationRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateSeen method with storeCreationRequest: {}", storeCreationRequest);
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(storeCreationRequest, List.of("seenIds"));
        List<Long> seenIds = storeCreationRequest.getSeenIds();
        for (long id : seenIds) {
            wholesaleStoreHbRepository.updateSeenNotifications(id); // Update operation
        }
        logger.debug("Completed updateSeen method");
    }

    @Transactional
    public List<WholesaleCategoryDto> getAllStoreCategory() {
        logger.debug("Starting getAllStoreCategory method");
        Sort sort = Sort.by("category").ascending();
        List<WholesaleCategoryDto> categories = wholesaleCategoryRepository.findAll(sort).stream().map(wholesaleCategoryMapper::toDto).toList();
        logger.debug("Completed getAllStoreCategory method");
        return categories;
    }

    @Transactional
    public List<WholesaleSubcategoryDto> getAllStoreSubCategories(int categoryId) {
        logger.debug("Starting getAllStoreSubCategories method with categoryId: {}", categoryId);
        List<WholesaleSubcategoryDto> subCategories = wholesaleSubCategoryRepository.getSubCategories(categoryId).stream().map(wholesaleSubCategoryMapper::toDto).toList();
        logger.debug("Completed getAllStoreSubCategories method");
        return subCategories;
    }

}
