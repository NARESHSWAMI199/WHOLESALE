package com.sales.admin.services;

import com.sales.admin.dto.CategoryDto;
import com.sales.admin.dto.StoreDto;
import com.sales.admin.dto.SubcategoryDto;
import com.sales.admin.mapper.CategoryMapper;
import com.sales.admin.mapper.StoreMapper;
import com.sales.admin.mapper.SubcategoryMapper;
import com.sales.admin.repositories.*;
import com.sales.claims.AuthUser;
import com.sales.request.*;
import com.sales.entities.*;
import com.sales.exceptions.MyException;
import com.sales.exceptions.NotFoundException;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import com.sales.utils.UploadImageValidator;
import com.sales.utils.Utils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.PermissionDeniedDataAccessException;
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
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

import static com.sales.helpers.PaginationHelper.getPageable;
import static com.sales.specifications.StoreSpecifications.*;


@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final ItemRepository itemRepository;
    private final StoreHbRepository storeHbRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final StoreSubCategoryRepository storeSubCategoryRepository;
    private final UserHbRepository userHbRepository;
    private final AddressHbRepository addressHbRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);
    private final StoreMapper storeMapper;
    private final CategoryMapper categoryMapper;
    private final SubcategoryMapper subcategoryMapper;

    @Value("${store.absolute}")
    String storeImagePath;


    public Page<StoreDto> getAllStore(StoreFilterRequest filters) {
        logger.debug("Entering getAllStore with filters: {}", filters);
        Specification<Store> specification = Specification.allOf(
                (containsName(filters.getSearchKey()).or(containsEmail(filters.getSearchKey())))
                        .and(greaterThanOrEqualFromDate(filters.getFromDate()))
                        .and(lessThanOrEqualToToDate(filters.getToDate()))
                        .and(isStatus(filters.getStatus()))
                        .and(hasSlug(filters.getSlug()))
        );
        Pageable pageable = getPageable(logger, filters);
        Page<Store> storePage = storeRepository.findAll(specification, pageable);
        List<Store> storeList = storePage.getContent();
        storeList.forEach(store -> store.setTotalStoreItems(itemRepository.totalItemCountByWholesaleId(store.getId())));
        storePage = new PageImpl<>(storeList, pageable, storePage.getTotalElements());
        logger.debug("Exiting getAllStore");
        return storePage.map(storeMapper::toDto);
    }


    public Map<String, Integer> getWholesaleCounts() {
        logger.debug("Entering getWholesaleCounts");
        Map<String, Integer> responseObj = new HashMap<>();
        responseObj.put("all", storeRepository.totalWholesaleCount());
        responseObj.put("active", storeRepository.optionWholesaleCount("A"));
        responseObj.put("deactive", storeRepository.optionWholesaleCount("D"));
        logger.debug("Exiting getWholesaleCounts");
        return responseObj;
    }


    public AddressRequest getAddressObjFromStore(StoreCreationRequest storeCreationRequest) {
        logger.debug("Entering getAddressObjFromStore with storeCreationRequest: {}", storeCreationRequest);
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setStreet(storeCreationRequest.getStreet());
        addressRequest.setZipCode(storeCreationRequest.getZipCode());
        addressRequest.setCity(storeCreationRequest.getCity());
        addressRequest.setState(storeCreationRequest.getState());
        addressRequest.setLatitude(storeCreationRequest.getLatitude());
        addressRequest.setAltitude(storeCreationRequest.getAltitude());
        logger.debug("Exiting getAddressObjFromStore");
        return addressRequest;
    }

    public Map<String, Object> getStoreCountByMonths(GraphRequest graphRequest) {
        logger.debug("Entering getStoreCountByMonths with graphRequest: {}", graphRequest);
        List<Integer> months = graphRequest.getMonths();
        months = (months == null || months.isEmpty()) ?
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12) : months;
        Integer year = graphRequest.getYear();
        Map<String, Object> monthsObj = new LinkedHashMap<>();
        for (Integer month : months) {
            monthsObj.put(getMonthName(month), storeRepository.totalStoreViaMonth(month, year));
        }
        logger.debug("Exiting getStoreCountByMonths");
        return monthsObj;
    }

    public String getMonthName(int month) {
        logger.debug("Entering getMonthName with month: {}", month);
        if (month <= 0 || month > 12) {
            return null;
        }
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("eng"));
        logger.debug("Exiting getMonthName");
        return monthName;
    }

    public void validateRequiredFieldsForStore(StoreCreationRequest storeCreationRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering validateRequiredFieldsForStore with storeCreationRequest: {}", storeCreationRequest);
        List<String> requiredFields = new ArrayList<>(List.of(
                "storeName",
                "storeEmail",
                "storePhone",
                "rating",
                "categoryId",
                "subCategoryId",
                "description"
        ));
        // if there is any required field null, then this will throw IllegalArgumentException
        Utils.checkRequiredFields(storeCreationRequest, requiredFields);
        logger.debug("Exiting validateRequiredFieldsForStore");
    }

    public void validateRequiredFieldsForCreateStore(StoreCreationRequest storeCreationRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering validateRequiredFieldsForCreateStore with storeCreationRequest: {}", storeCreationRequest);
        List<String> requiredFields = new ArrayList<>(List.of("userSlug"));
        // if there is any required field null, then this will throw IllegalArgumentException
        Utils.checkRequiredFields(storeCreationRequest, requiredFields);
        logger.debug("Exiting validateRequiredFieldsForCreateStore");
    }


    @Transactional(rollbackFor = {MyException.class, IllegalArgumentException.class, RuntimeException.class})
    public Map<String, Object> createOrUpdateStore(StoreCreationRequest storeCreationRequest, AuthUser loggedUser, String path) throws MyException, IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering createOrUpdateStore with storeCreationRequest: {}, loggedUser: {}, path: {}", storeCreationRequest, loggedUser, path);
        Map<String, Object> responseObj = new HashMap<>();
        // if there is any required field null, then this will throw IllegalArgumentException
        validateRequiredFieldsForStore(storeCreationRequest);
        try {
            StoreCategory storeCategory = storeCategoryRepository.findById(storeCreationRequest.getCategoryId()).orElseThrow(() -> new NotFoundException("Store category not found."));
            storeCreationRequest.setStoreCategory(storeCategory);
            StoreSubCategory storeSubCategory = storeSubCategoryRepository.findById(storeCreationRequest.getSubCategoryId()).orElseThrow(() -> new NotFoundException("Store subcategory not found."));
            storeCreationRequest.setStoreSubCategory(storeSubCategory);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid arguments for category and subcategory");
        }

        if (!Utils.isEmpty(storeCreationRequest.getStoreSlug()) || path.contains("update")) { // We are going to update the store.
            logger.debug("We are going to update the store.");
            // if there is any required field null, then this will throw IllegalArgumentException
            Utils.checkRequiredFields(storeCreationRequest, List.of("storeSlug"));

            String storeName = Utils.isValidName(storeCreationRequest.getStoreName(), ConstantResponseKeys.STORE);
            storeCreationRequest.setStoreName(storeName);
            // If we found any issue with email and mobile, this will throw exception
            Utils.mobileAndEmailValidation(storeCreationRequest.getStoreEmail(), storeCreationRequest.getStorePhone(), "Not a valid store's _ recheck your and store's _.");
            updateStoreImage(storeCreationRequest.getStorePic(), storeCreationRequest.getStoreSlug());

            // before update store and store's address get address id from store
            Integer addressId = storeRepository.getAddressIdBySlug(storeCreationRequest.getStoreSlug());
            if (addressId == null)
                throw new IllegalArgumentException("Store address not found.");  // wrong wholesale slug.
            storeCreationRequest.setAddressId(addressId);

            int isUpdated = updateStore(storeCreationRequest, loggedUser);
            if (isUpdated > 0) {
                responseObj.put(ConstantResponseKeys.MESSAGE, "Successfully updated.");
                responseObj.put(ConstantResponseKeys.STATUS, 200);
            } else {
                responseObj.put(ConstantResponseKeys.MESSAGE, "Nothing found to updated.");
                responseObj.put(ConstantResponseKeys.STATUS, 404);
            }
        } else {  // We are going to create a store.
            logger.debug("We are going to create the store.");
            // if there is any required field null, then this will throw IllegalArgumentException
            validateRequiredFieldsForCreateStore(storeCreationRequest);

            // if there is any issue, this will throw IllegalArgumentException
            Utils.mobileAndEmailValidation(
                    storeCreationRequest.getStoreEmail(),
                    storeCreationRequest.getStorePhone(),
                    "Not a valid store's _ recheck your and store's _."
            );

            String storeName = Utils.isValidName(storeCreationRequest.getStoreName(), ConstantResponseKeys.STORE);
            storeCreationRequest.setStoreName(storeName);
            Store createdStore = createStore(storeCreationRequest, loggedUser);
            StoreDto storeDto = storeMapper.toDto(createdStore);
            responseObj.put(ConstantResponseKeys.RES, storeDto);
            responseObj.put(ConstantResponseKeys.MESSAGE, "Store successfully inserted.");
            responseObj.put(ConstantResponseKeys.STATUS, 201);
        }
        logger.debug("Exiting createOrUpdateStore");
        return responseObj;

    }


    public void validateRequiredFieldsForCreateAddress(AddressRequest addressRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering validateRequiredFieldsForCreateAddress with addressRequest: {}", addressRequest);
        List<String> requiredFields = new ArrayList<>(List.of("street", "zipCode", "city", "state"));
        // if there is any required field null, then this will throw IllegalArgumentException
        Utils.checkRequiredFields(addressRequest, requiredFields);
        logger.debug("Exiting validateRequiredFieldsForCreateAddress");
    }


    @Transactional(rollbackFor = {MyException.class, IllegalArgumentException.class, RuntimeException.class})
    public Store createStore(StoreCreationRequest storeCreationRequest, AuthUser loggedUser) throws MyException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering createStore with storeCreationRequest: {}, loggedUser: {}", storeCreationRequest, loggedUser);
        /** inserting address during create a wholesale */
        AddressRequest addressRequest = getAddressObjFromStore(storeCreationRequest);
        // if there is any required field null then this will throw IllegalArgumentException
        validateRequiredFieldsForCreateAddress(addressRequest);
        Address address = addressService.insertAddress(addressRequest, loggedUser);

        /** @END inserting  address during create a wholesale */
        Optional<User> storeOwner = userRepository.findByWholesalerSlug(storeCreationRequest.getUserSlug());
        if (storeOwner.isEmpty())
            throw new PermissionDeniedDataAccessException("User must be wholesaler.", new Exception());


        // Saving the store data
        Store store = new Store(loggedUser);
        store.setUser(storeOwner.get());
        store.setStoreName(storeCreationRequest.getStoreName());
        store.setEmail(storeCreationRequest.getStoreEmail());
        store.setAddress(address);
        store.setDescription(storeCreationRequest.getDescription());
        store.setPhone(storeCreationRequest.getStorePhone());
        store.setRating(storeCreationRequest.getRating());
        store.setStoreCategory(storeCreationRequest.getStoreCategory());
        store.setStoreSubCategory(storeCreationRequest.getStoreSubCategory());
        logger.debug("Exiting createStore");
        return storeRepository.save(store);
    }

    @Transactional(rollbackFor = {MyException.class, IllegalArgumentException.class, RuntimeException.class})
    public int updateStore(StoreCreationRequest storeCreationRequest, AuthUser loggedUser) {
        logger.debug("Entering updateStore with storeCreationRequest: {}, loggedUser: {}", storeCreationRequest, loggedUser);
        AddressRequest address = new AddressRequest();
        address.setStreet(storeCreationRequest.getStreet());
        address.setZipCode(storeCreationRequest.getZipCode());
        address.setState(storeCreationRequest.getState());
        address.setCity(storeCreationRequest.getCity());
        address.setState(storeCreationRequest.getState());
        address.setAddressId(storeCreationRequest.getAddressId());
        int isUpdatedAddress = addressHbRepository.updateAddress(address, loggedUser);
        if (isUpdatedAddress < 1) return isUpdatedAddress;
        int result = storeHbRepository.updateStore(storeCreationRequest, loggedUser);
        logger.debug("Exiting updateStore");
        return result;
    }

    @Transactional(rollbackFor = {MyException.class, IllegalArgumentException.class, RuntimeException.class, Exception.class})
    public int deleteStoreBySlug(DeleteRequest deleteRequest, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering deleteStoreBySlug with deleteRequest: {}, loggedUser: {}", deleteRequest, loggedUser);
        // Validate required fields. if we found any required field this will throw IllegalArgumentException
        Utils.checkRequiredFields(deleteRequest, List.of("slug"));

        String slug = deleteRequest.getSlug();
        Store store = storeRepository.findStoreBySlug(slug);
        if (store == null) throw new NotFoundException("No store found to delete.");
        User user = store.getUser();
        if (user != null) userHbRepository.deleteUserBySlug(user.getSlug());
        int result = storeHbRepository.deleteStore(slug, loggedUser);
        logger.debug("Exiting deleteStoreBySlug");
        return result;

    }

    public void deleteStoreByUserId(int userId) {
        logger.debug("Entering deleteStoreByUserId with userId: {}", userId);
        storeHbRepository.deleteStore(userId);
        logger.debug("Exiting deleteStoreByUserId");
    }


    @Transactional
    public Store getStoreDetails(String slug) {
        logger.debug("Entering getStoreDetails with slug: {}", slug);
        Store store = storeRepository.findStoreBySlug(slug);
        logger.debug("Exiting getStoreDetails");
        return store;
    }

    public Integer getStoreIdByStoreSlug(String slug) {
        logger.debug("Entering getStoreIdByStoreSlug with slug: {}", slug);
        Integer storeId = storeRepository.getStoreIdByStoreSlug(slug);
        logger.debug("Exiting getStoreIdByStoreSlug");
        return storeId;
    }

    public Store getStoreByUserSlug(String userSlug) {
        logger.debug("Entering getStoreByUserSlug with userSlug: {}", userSlug);
        if (Utils.isEmpty(userSlug)) throw new IllegalArgumentException("User slug can't be null or blank.");
        User user = userRepository.findUserBySlug(userSlug);
        if (user == null) throw new NotFoundException("No user found.");
        Store store = storeRepository.findStoreByUserId(user.getId());
        if (store == null) throw new NotFoundException("Store not found.");
        // setting total items to with store detail
        store.setTotalStoreItems(itemRepository.totalItemCountByWholesaleId(store.getId()));
        logger.debug("Exiting getStoreByUserSlug");
        return store;
    }


    @Transactional(rollbackFor = {IllegalArgumentException.class, MyException.class, RuntimeException.class, Exception.class})
    public int updateStatusBySlug(StatusRequest statusRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering updateStatusBySlug with statusRequest: {}", statusRequest);
        // Validate required fields. if we found any required field this will throw IllegalArgumentException
        Utils.checkRequiredFields(statusRequest, List.of("status", "slug"));

        switch (statusRequest.getStatus()) {
            case "A", "D":
                Store store = storeRepository.findStoreBySlug(statusRequest.getSlug());
                if (store == null) throw new NotFoundException("No store found to update.");
                String status = statusRequest.getStatus();
                // updating store user status also
                store.getUser().setStatus(statusRequest.getStatus());
                store.setStatus(status);
                int result = storeRepository.save(store).getId();
                logger.debug("Exiting updateStatusBySlug");
                return result;
            default:
                throw new IllegalArgumentException("Status must be A or D.");
        }
    }


    @Transactional(rollbackFor = {IllegalArgumentException.class, MyException.class, RuntimeException.class, Exception.class})
    public int updateStoreImage(MultipartFile storeImage, String slug) throws MyException, IOException {
        logger.debug("Entering updateStoreImage with storeImage: {}, slug: {}", storeImage, slug);
        if (storeImage != null) {
            if (UploadImageValidator.isValidImage(storeImage, GlobalConstant.bannerMinWidth,
                    GlobalConstant.bannerMinHeight, GlobalConstant.bannerMaxWidth, GlobalConstant.bannerMaxHeight,
                    GlobalConstant.allowedAspectRatios, GlobalConstant.allowedFormats)) {
                String fileOriginalName = Objects.requireNonNull(storeImage.getOriginalFilename()).replaceAll(" ", "_");
                String dirPath = storeImagePath + GlobalConstant.PATH_SEPARATOR + slug + GlobalConstant.PATH_SEPARATOR;
                File dir = new File(dirPath);
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dirPath + fileOriginalName);
                storeImage.transferTo(file);
                int result = storeHbRepository.updateStoreAvatar(slug, fileOriginalName);
                logger.debug("Exiting updateStoreImage");
                return result;
            } else {
                throw new IllegalArgumentException("Image is not fit in accept ratio. please resize you image before upload.");
            }
        }
        logger.debug("Exiting updateStoreImage");
        return 0;
    }


    public List<CategoryDto> getAllStoreCategory(SearchFilters searchFilters) {
        logger.debug("Entering getAllStoreCategory with searchFilters: {}", searchFilters);
        Sort sort = searchFilters.getOrder().equals("asc") ?
                Sort.by(searchFilters.getOrderBy()).ascending() :
                Sort.by(searchFilters.getOrderBy()).descending();
        List<CategoryDto> result = storeCategoryRepository.findAll(sort).stream().map(categoryMapper::toDto).toList();
        logger.debug("Exiting getAllStoreCategory");
        return result;
    }


    public List<SubcategoryDto> getAllStoreSubCategories(SubCategoryFilterRequest searchFilters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering getAllStoreSubCategories with searchFilters: {}", searchFilters);
        // Validating required fields if found any required field is null, this will throw IllegalArgumentException
        Utils.checkRequiredFields(searchFilters, List.of("categoryId"));
        Sort sort = Sort.by(searchFilters.getOrderBy());
        sort = searchFilters.getOrder().equals("asc") ? sort.ascending() : sort.descending();
        List<SubcategoryDto> result = storeSubCategoryRepository.getSubCategories(searchFilters.getCategoryId(), sort).stream().map(subcategoryMapper::toDto).toList();
        logger.debug("Exiting getAllStoreSubCategories");
        return result;
    }


    @Transactional(rollbackFor = {MyException.class, IllegalArgumentException.class, RuntimeException.class})
    public StoreCategory saveOrUpdateStoreCategory(CategoryRequest categoryRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering saveOrUpdateStoreCategory with categoryRequest: {}", categoryRequest);
        // Validate required fields if we found any given field is null, then this will throw Exception
        Utils.checkRequiredFields(categoryRequest, List.of("category", "icon"));

        StoreCategory storeCategory = new StoreCategory();
        if (categoryRequest.getId() != null)
            storeCategory.setId(categoryRequest.getId());
        storeCategory.setCategory(categoryRequest.getCategory());
        storeCategory.setIcon(categoryRequest.getIcon());
        StoreCategory result = storeCategoryRepository.save(storeCategory);
        logger.debug("Exiting saveOrUpdateStoreCategory");
        return result;
    }

    @Transactional(rollbackFor = {MyException.class, IllegalArgumentException.class, RuntimeException.class})
    public StoreSubCategory saveOrUpdateStoreSubCategory(SubCategoryRequest subCategoryRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering saveOrUpdateStoreSubCategory with subCategoryRequest: {}", subCategoryRequest);
        // Validate required fields if we found any given field is null, then this will throw Exception
        Utils.checkRequiredFields(subCategoryRequest, List.of("categoryId", "subcategory", "icon"));
        StoreSubCategory storeSubCategory = new StoreSubCategory();
        if (subCategoryRequest.getId() != null)
            storeSubCategory.setId(subCategoryRequest.getId());
        storeSubCategory.setCategoryId(subCategoryRequest.getCategoryId());
        storeSubCategory.setSubcategory(subCategoryRequest.getSubcategory());
        storeSubCategory.setIcon(subCategoryRequest.getIcon());
        storeSubCategory.setUpdatedAt(Utils.getCurrentMillis());
        StoreSubCategory result = storeSubCategoryRepository.save(storeSubCategory);
        logger.debug("Exiting saveOrUpdateStoreSubCategory");
        return result;
    }


    public CategoryDto getStoreCategoryById(int categoryId) {
        logger.debug("Entering getStoreCategoryById with categoryId: {}", categoryId);
        StoreCategory result = storeCategoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Store category not found."));
        logger.debug("Exiting getStoreCategoryById");
        return categoryMapper.toDto(result);
    }


    public int deleteStoreCategory(DeleteRequest deleteRequest, AuthUser user) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering deleteStoreCategory with deleteRequest: {}, user: {}", deleteRequest, user);
        // Validating required fields if they are null, this will throw an Exception
        Utils.checkRequiredFields(deleteRequest, List.of("slug"));
        if (!user.getUserType().equals("SA"))
            throw new PermissionDeniedDataAccessException("Only super admin can delete a store category.", new Exception());
        String slug = deleteRequest.getSlug();
        Integer categoryId = storeHbRepository.getStoreCategoryIdBySLug(slug);
        if (categoryId == null) throw new NotFoundException("Store's category not found.");
        storeHbRepository.switchCategoryToOther(categoryId);  // before delete category assign store to the other category.
        int result = storeHbRepository.deleteStoreCategory(slug);
        logger.debug("Exiting deleteStoreCategory");
        return result;
    }

    public int deleteStoreSubCategory(DeleteRequest deleteRequest, AuthUser user) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering deleteStoreSubCategory with deleteRequest: {}, user: {}", deleteRequest, user);
        // Validating required fields if they are null this will throw an Exception
        Utils.checkRequiredFields(deleteRequest, List.of("slug"));
        String slug = deleteRequest.getSlug();
        if (!user.getUserType().equals("SA"))
            throw new PermissionDeniedDataAccessException("Only super admin can delete a store subcategory.", new Exception());
        Integer subCategoryId = storeSubCategoryRepository.getStoreSubCategoryIdBySlug(slug);
        if (subCategoryId == null) throw new NotFoundException("Store's subcategory not found.");
        storeHbRepository.switchSubCategoryToOther(subCategoryId);
        int result = storeHbRepository.deleteStoreSubCategory(slug);
        logger.debug("Exiting deleteStoreSubCategory");
        return result;
    }


}
