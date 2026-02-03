package com.sales.wholesaler.services;

import com.sales.admin.repositories.AddressRepository;
import com.sales.claims.AuthUser;
import com.sales.dto.AddressRequest;
import com.sales.dto.SearchFilters;
import com.sales.dto.StoreRequest;
import com.sales.entities.*;
import com.sales.exceptions.MyException;
import com.sales.exceptions.NotFoundException;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

import static com.sales.helpers.PaginationHelper.getPageable;
import static com.sales.specifications.ItemReviewSpecifications.isUserId;
import static com.sales.specifications.ItemReviewSpecifications.isWholesaleId;
import static com.sales.utils.Utils.getCurrentMillis;

@Service
@RequiredArgsConstructor
public class WholesaleStoreService  {

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
    private static final Logger logger = LoggerFactory.getLogger(WholesaleStoreService.class);

    @Value("${store.absolute}")
    String storeImagePath;

    @Transactional(rollbackOn = {IllegalArgumentException.class, MyException.class, RuntimeException.class})
    public Map<String, Object> updateStoreBySlug(StoreRequest storeRequest, AuthUser loggedUser) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateStoreBySlug method with storeRequest: {}, loggedUser: {}", storeRequest, loggedUser);

        // Validating required fields. If there we found any required field is null, this will throw an Exception
        Utils.checkRequiredFields(storeRequest, List.of("storeName", "storeEmail", "storePhone", "categoryId", "subCategoryId"));

        Map<String, Object> responseObj = new HashMap<>();
        String storeName = Utils.isValidName(storeRequest.getStoreName(), ConstantResponseKeys.STORE);
        storeRequest.setStoreName(storeName);

        /* '_' replaced by actual error message in mobileAndEmailValidation */
        Utils.mobileAndEmailValidation(storeRequest.getStoreEmail(), storeRequest.getStorePhone(), "Not a valid _");

        try {
            StoreCategory storeCategory = wholesaleCategoryRepository.findById(storeRequest.getCategoryId()).orElseThrow(() -> new NotFoundException("Store category not found."));
            storeRequest.setStoreCategory(storeCategory);
            StoreSubCategory storeSubCategory = wholesaleSubCategoryRepository.findById(storeRequest.getSubCategoryId()).orElseThrow(() -> new NotFoundException("Store subcategory not found."));
            storeRequest.setStoreSubCategory(storeSubCategory);
        } catch (Exception e) {
            throw new MyException("Invalid arguments for category and subcategory");
        }
        Store store = getStoreByUserId(loggedUser.getId());
        String slug = store.getSlug();
        storeRequest.setStoreSlug(slug);

        // before update store and store's address get address id from store
        Integer addressId = wholesaleStoreRepository.getAddressIdBySlug(storeRequest.getStoreSlug());
        if (addressId == null) throw new IllegalArgumentException("No store found to update.");  // wrong wholesale slug.
        storeRequest.setAddressId(addressId);

        String imageName = getStoreImagePath(storeRequest.getStorePic(), slug);
        if (imageName != null) {
            storeRequest.setStoreAvatar(imageName);
        } else {
            storeRequest.setStoreAvatar(store.getAvtar());
        }
        int isUpdated = updateStore(storeRequest, loggedUser); // Update operation
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "successfully updated.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No store found to update");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        logger.debug("Completed updateStoreBySlug method");
        return responseObj;
    }

    @Transactional(rollbackOn = {IllegalArgumentException.class, MyException.class, RuntimeException.class})
    public int updateStore(StoreRequest storeRequest, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateStore method with storeRequest: {}, loggedUser: {}", storeRequest, loggedUser);
        AddressRequest address = new AddressRequest();
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(storeRequest, List.of("street", "zipCode", "city", "state"));
        address.setStreet(storeRequest.getStreet());
        address.setZipCode(storeRequest.getZipCode());
        address.setCity(storeRequest.getCity());
        address.setState(storeRequest.getState());
        address.setAddressId(storeRequest.getAddressId());
        int isUpdatedAddress = wholesaleAddressHbRepository.updateAddress(address, loggedUser); // Update operation
        if (isUpdatedAddress < 1) return isUpdatedAddress;
        int isUpdatedStore = wholesaleStoreHbRepository.updateStore(storeRequest, loggedUser); // Update operation
        logger.debug("Completed updateStore method");
        return isUpdatedStore;
    }


    @Transactional
    public WholesaleStoreDto getStoreDtoByUserSlug(String slug) {
        logger.debug("Starting getStoreByUserSlug method with slug: {}", slug);
        Store store = wholesaleStoreRepository.getStoreIdByUserSlug(slug);
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

    @Transactional
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
                throw new MyException("Image is not fit in accept ratio. please resize your image before upload.");
            }
        }
        return null;
    }

    @Transactional(rollbackOn = {MyException.class, IllegalArgumentException.class, RuntimeException.class, Exception.class})
    public Store createStore(StoreRequest storeRequest, AuthUser loggedUser) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting createStore method with storeRequest: {}, loggedUser: {}", storeRequest, loggedUser);

        // Validating required fields. If their we found any required field is null, this will throw an Exception
        Utils.checkRequiredFields(storeRequest, List.of("storeName", "storePic", "storeEmail", "storePhone", "categoryId", "subCategoryId"));

        /* '_' replaced by actual error message in mobileAndEmailValidation */
        Utils.mobileAndEmailValidation(storeRequest.getStoreEmail(), storeRequest.getStorePhone(), "Not a valid _");

        try {
            StoreCategory storeCategory = wholesaleCategoryRepository.findById(storeRequest.getCategoryId()).orElseThrow(() -> new NotFoundException("Store category not found."));
            storeRequest.setStoreCategory(storeCategory);
            StoreSubCategory storeSubCategory = wholesaleSubCategoryRepository.findById(storeRequest.getSubCategoryId()).orElseThrow(() -> new NotFoundException("Store subcategory not found."));
            storeRequest.setStoreSubCategory(storeSubCategory);
        } catch (Exception e) {
            throw new MyException("Invalid arguments for category and subcategory");
        }

        /* inserting  address during create a wholesale */
        AddressRequest addressRequest = getAddressObjFromStore(storeRequest);
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(addressRequest, List.of("street", "zipCode", "city", "state"));
        Address address = insertAddress(addressRequest, loggedUser); // Create operation

        Store store = new Store(loggedUser);
        store.setUser(User.builder().id(loggedUser.getId()).build());
        store.setStoreName(storeRequest.getStoreName());
        store.setEmail(storeRequest.getStoreEmail());
        store.setAddress(address);
        store.setDescription(storeRequest.getDescription());
        store.setPhone(storeRequest.getStorePhone());
        store.setRating(0f);
        store.setStoreCategory(storeRequest.getStoreCategory());
        store.setStoreSubCategory(storeRequest.getStoreSubCategory());
        Store insertedStore = wholesaleStoreRepository.save(store); // Create operation
        String imageName = getStoreImagePath(storeRequest.getStorePic(), insertedStore.getSlug());
        if (imageName != null) {
            store.setAvtar(imageName); /** I know save function called before set this, but it will save automatically due to same transaction */
        } else {
            throw new MyException("Store image can't be blank.");
        }
        logger.debug("Completed createStore method");
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

    public AddressRequest getAddressObjFromStore(StoreRequest storeRequest) {
        logger.debug("Starting getAddressObjFromStore method with storeRequest: {}", storeRequest);
        AddressRequest addressRequest = AddressRequest.builder()
            .street(storeRequest.getStreet())
            .zipCode(storeRequest.getZipCode())
            .city(storeRequest.getCity())
            .state(storeRequest.getState())
            .latitude(storeRequest.getLatitude())
            .altitude(storeRequest.getAltitude())
            .build();
        logger.debug("Completed getAddressObjFromStore method");
        return addressRequest;
    }

    public Page<WholesaleStoreNotificationDto> getAllStoreNotification(SearchFilters filters, AuthUser loggedUser) {
        logger.debug("Starting getAllStoreNotification method with filters: {}, loggedUser: {}", filters, loggedUser);
        Integer storeId = wholesaleStoreRepository.getStoreIdByUserId(loggedUser.getId());
        Specification<StoreNotifications> specification = Specification.allOf(isUserId(loggedUser.getId()).or(isWholesaleId(storeId)));
        Pageable pageable = getPageable(logger,filters);
        Page<StoreNotifications> notificationsPage = wholesaleNotificationRepository.findAll(specification, pageable);
        List<WholesaleStoreNotificationDto> notificationDtoList = notificationsPage.getContent().stream().map(wholesaleStoreNotificationMapper::toDto).toList();
        Page<WholesaleStoreNotificationDto> notifications = new PageImpl<>(notificationDtoList, pageable, notificationsPage.getTotalElements());
        logger.debug("Completed getAllStoreNotification method");
        return notifications;
    }

    public void updateSeen(StoreRequest storeRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateSeen method with storeRequest: {}", storeRequest);
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(storeRequest, List.of("seenIds"));
        List<Long> seenIds = storeRequest.getSeenIds();
        for (long id : seenIds) {
            wholesaleStoreHbRepository.updateSeenNotifications(id); // Update operation
        }
        logger.debug("Completed updateSeen method");
    }

    public List<WholesaleCategoryDto> getAllStoreCategory() {
        logger.debug("Starting getAllStoreCategory method");
        Sort sort = Sort.by("category").ascending();
        List<WholesaleCategoryDto> categories = wholesaleCategoryRepository.findAll(sort).stream().map(wholesaleCategoryMapper::toDto).toList();
        logger.debug("Completed getAllStoreCategory method");
        return categories;
    }

    public List<WholesaleSubcategoryDto> getAllStoreSubCategories(int categoryId) {
        logger.debug("Starting getAllStoreSubCategories method with categoryId: {}", categoryId);
        List<WholesaleSubcategoryDto> subCategories = wholesaleSubCategoryRepository.getSubCategories(categoryId).stream().map(wholesaleSubCategoryMapper::toDto).toList();
        logger.debug("Completed getAllStoreSubCategories method");
        return subCategories;
    }

}
