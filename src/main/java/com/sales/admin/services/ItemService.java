package com.sales.admin.services;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sales.admin.dto.CategoryDto;
import com.sales.admin.dto.ItemDto;
import com.sales.admin.dto.SubcategoryDto;
import com.sales.admin.mapper.CategoryMapper;
import com.sales.admin.mapper.ItemMapper;
import com.sales.admin.mapper.SubcategoryMapper;
import com.sales.admin.repositories.*;
import com.sales.claims.AuthUser;
import com.sales.entities.*;
import com.sales.exceptions.MyException;
import com.sales.exceptions.NotFoundException;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import com.sales.global.ResponseMessages;
import com.sales.global.USER_TYPES;
import com.sales.request.*;
import com.sales.request.enums.ITEM_LABEL;
import com.sales.request.enums.ITEM_STOCK;
import com.sales.requests.ItemRequest;
import com.sales.utils.UploadImageValidator;
import com.sales.utils.Utils;
import com.sales.utils.WriteExcelUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

import static com.sales.specifications.ItemsSpecifications.*;

@Service
@RequiredArgsConstructor
public class ItemService {


    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    private final ItemRepository itemRepository;
    private final WriteExcelUtil writeExcel;
    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemSubCategoryRepository itemSubCategoryRepository;
    private final StoreRepository storeRepository;
    private final ItemHbRepository itemHbRepository;
    private final StoreHbRepository storeHbRepository;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final ItemMapper itemMapper;
    private final CategoryMapper categoryMapper;
    private final SubcategoryMapper subcategoryMapper;
    @Value("${item.absolute}")
    String itemImagePath;


    @Transactional
    public Page<ItemDto> getAllItems(ItemFilterRequest searchFilters, AuthUser loggedUser) {
        logger.debug("Entering getAllItems with searchFilters: {}", searchFilters);
        Integer storeId = storeRepository.getStoreIdByStoreSlug(searchFilters.getStoreSlug());
        Sort sort = searchFilters.getOrder().equalsIgnoreCase("asc") ?
                Sort.by(searchFilters.getOrderBy()).ascending() :
                Sort.by(searchFilters.getOrderBy()).descending();
        Specification<Item> specification = Specification.allOf(
                containsName(searchFilters.getSearchKey().trim())
                        .and(hasSlug(searchFilters.getSlug()))
                        .and(isWholesale(storeId, loggedUser.getUserType()))
                        .and(isStatus(searchFilters.getStatus()))
                        .and(inStock(searchFilters.getInStock()))
                        .and(greaterThanOrEqualFromDate(searchFilters.getFromDate()))
                        .and(lessThanOrEqualToToDate(searchFilters.getToDate()))
        );
        Pageable pageable = PageRequest.of(searchFilters.getPageNumber(), searchFilters.getSize(), sort);
        Page<Item> result = itemRepository.findAll(specification, pageable);
        logger.debug("Exiting getAllItems");
        return result.map(itemMapper::toDto);
    }


    @Transactional
    public String createItemsExcelSheet(ItemFilterRequest searchFilters, String wholesaleSlug, AuthUser loggedUser) throws IOException {
        logger.debug("Entering createItemsExcelSheet with searchFilters: {}", searchFilters);

        Integer storeId = wholesaleSlug != null ? storeRepository.getStoreIdByStoreSlug(wholesaleSlug) : null;
        Specification<Item> specification = Specification.allOf(
                containsName(searchFilters.getSearchKey().trim())
                        .and(isWholesale(storeId, loggedUser.getUserType()))
                        .and(isStatus(searchFilters.getStatus()))
                        .and(inStock(searchFilters.getInStock()))
                        .and(greaterThanOrEqualFromDate(searchFilters.getFromDate()))
                        .and(lessThanOrEqualToToDate(searchFilters.getToDate()))
        );
        List<ItemDto> itemsList = itemRepository.findAll(specification).stream().map(itemMapper::toDto).toList();
        Map<String, List<Object>> result = new HashMap<>();
        for (ItemDto item : itemsList) {
            Gson itemsGson = new GsonBuilder().serializeNulls().create();
            String items = itemsGson.toJson(item);
            Map<String, Object> itemMap = new Gson().fromJson(items, Map.class);
            itemMap.forEach((key, value) -> {
                if (key.equals("wholesale")) {
                    // skip...
                } else if (result.containsKey(key.toUpperCase())) {
                    result.get(key.toUpperCase()).add(itemMap.get(key));
                } else {
                    List<Object> valueList = new ArrayList<>();
                    valueList.add(value);
                    result.put(key.toUpperCase(), valueList);
                }
            });
        }
        int totalItem = itemsList.size();
        String folderName = wholesaleSlug != null ? wholesaleSlug : loggedUser.getSlug();
        // When we're creating all items, excel without a specific user wholesale or store from admin pannel
        if (folderName == null) folderName = loggedUser.getSlug();
        String filePath = writeExcel.createExcelSheet(result, totalItem, GlobalConstant.HEADERS_FOR_ITEMS, folderName);
        logger.debug("Exiting createItemsExcelSheet");
        return filePath;
    }

    public Map<String, Integer> getItemCounts() {
        logger.debug("Entering getItemCounts");
        Map<String, Integer> responseObj = new HashMap<>();
        responseObj.put("all", itemRepository.totalItemCount());
        responseObj.put("active", itemRepository.optionItemCount("A"));
        responseObj.put("deactive", itemRepository.optionItemCount("D"));
        logger.debug("Exiting getItemCounts");
        return responseObj;
    }


    @Transactional
    public ItemDto findItemDtoBySlug(String slug) {
        logger.debug("Entering findItemDtoBySlug with slug: {}", slug);
        Item result = itemRepository.findItemBySlug(slug);
        if (result == null) {
            return null;
        }
        result.setStoreSlug(storeRepository.findStoreSlugByStoreId(result.getWholesaleId()));
        logger.debug("Exiting findItemDtoBySlug");
        return itemMapper.toDto(result);
    }


    public Item findItemBySlug(String slug) {
        logger.debug("Entering findItemBySLug with slug: {}", slug);
        Item result = itemRepository.findItemBySlug(slug);
        logger.debug("Exiting findItemBySLug");
        return result;
    }


    public void validateRequiredFields(ItemRequest itemRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering validateRequiredFields with itemRequest: {}", itemRequest);
        // if there is any required field null, then this will throw IllegalArgumentException
        Utils.checkRequiredFields(itemRequest, List.of(
                "name",
                "price",
                "discount",
                "description",
//                "capacity",
                "categoryId",
                "subCategoryId"
        ));
        logger.debug("Exiting validateRequiredFields");
    }

    public void validateRequiredFieldsBeforeCreateItem(ItemRequest itemRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering validateRequiredFieldsBeforeCreateItem with itemRequest: {}", itemRequest);
        /** @Note during creation, we are checking only extra required params  */
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(itemRequest, List.of(
                "wholesaleSlug",
                "rating",
                "inStock",
                "label",
                "newItemImages"
        ));
        logger.debug("Exiting validateRequiredFieldsBeforeCreateItem");
    }

    @Transactional(rollbackFor = {MyException.class, IllegalArgumentException.class, RuntimeException.class,})
    public Map<String, Object> createOrUpdateItem(ItemRequest itemRequest, AuthUser loggedUser, String path) throws InvocationTargetException, NoSuchMethodException, IOException, IllegalAccessException {
        logger.debug("Entering createOrUpdateItem with itemRequest: {}, loggedUser: {}, path: {}", itemRequest, loggedUser, path);
        // if there is any required field null, then this will throw IllegalArgumentException
        validateRequiredFields(itemRequest);

        // Validate inStock
        if (!(ITEM_STOCK.OUT_OF_STOCK.getStock().equals(itemRequest.getInStock()) || ITEM_STOCK.INSTOCK.getStock().equals(itemRequest.getInStock())))
            throw new IllegalArgumentException(ResponseMessages.INSTOCK_MUST_BE_Y_OR_N);
        // Validate label
        if (!(ITEM_LABEL.NEW.getLabel().equals(itemRequest.getLabel()) || ITEM_LABEL.OLD.getLabel().equals(itemRequest.getLabel())))
            throw new IllegalArgumentException(ResponseMessages.LABEL_MUST_BE_O_OR_N);
        // Validate price and discount
        if (itemRequest.getPrice() < itemRequest.getDiscount() || itemRequest.getDiscount() < 0)
            throw new IllegalArgumentException(ResponseMessages.DISCOUNT_CAN_T_BE_GREATER_THEN_PRICE_AND_CAN_T_BE_LESS_THEN_0);

        // Verify item name syntax
        String itemName = Utils.isValidName(itemRequest.getName(), "item");
        itemRequest.setName(itemName);

        // retrieve category and subcategory
        ItemCategory itemCategory = itemCategoryRepository.findById(itemRequest.getCategoryId()).orElseThrow(() -> new NotFoundException("Item category not found."));
        ItemSubCategory itemSubCategory = itemSubCategoryRepository.findById(itemRequest.getSubCategoryId()).orElseThrow(() -> new NotFoundException("Item subcategory not found."));
        itemRequest.setItemCategory(itemCategory);
        itemRequest.setItemSubCategory(itemSubCategory);

        Map<String, Object> responseObj = new HashMap<>();

        // Going to update item
        if (!Utils.isEmpty(itemRequest.getSlug()) || path.contains("update")) {
            logger.info("We are going to update the item.");
            Integer storeId = storeRepository.getStoreIdByStoreSlug(itemRequest.getWholesaleSlug());
            if (storeId != null) {
                itemRequest.setStoreId(storeId);
            } else {
                throw new NotFoundException(ResponseMessages.NO_STORE_FOUND_FOR_UPDATE_THIS_ITEM);
            }
            int isUpdated = updateItem(itemRequest, loggedUser);
            // updating item images
            updateStoreImage(itemRequest.getPreviousItemImages(), itemRequest.getNewItemImages(), itemRequest.getSlug(), "update");
            if (isUpdated > 0) {
                responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SUCCESSFULLY_UPDATED);
                responseObj.put(ConstantResponseKeys.STATUS, 200);
            } else {
                responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.NO_ITEM_FOUND_TO_UPDATE);
                responseObj.put(ConstantResponseKeys.STATUS, 404);
            }
        } else { // Going to create item
            logger.debug("We are going to create the item.");
            // if there is any required field null, then this will throw IllegalArgumentException
            validateRequiredFieldsBeforeCreateItem(itemRequest);
            Item createdItem = createItem(itemRequest, loggedUser);
            ItemDto cratedItemDto = itemMapper.toDto(createdItem);
            responseObj.put(ConstantResponseKeys.RES, cratedItemDto);
            responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SUCCESSFULLY_INSERTED);
            responseObj.put(ConstantResponseKeys.STATUS, 201);
        }
        logger.debug("Exiting createOrUpdateItem");
        return responseObj;

    }


    @Transactional
    public Item createItem(ItemRequest itemRequest, AuthUser loggedUser) throws IOException {
        logger.debug("Entering createItem with itemRequest: {}, loggedUser: {}", itemRequest, loggedUser);
        Item item = new Item();
        Store store = storeRepository.findStoreBySlug(itemRequest.getWholesaleSlug());
        if (store == null) throw new IllegalArgumentException(ResponseMessages.NOT_A_VALID_STORE);
        User userForUpdate = User.builder()
                .id(loggedUser.getId())
                .username(loggedUser.getUsername())
                .build();
        String slug = UUID.randomUUID().toString();
        item.setWholesaleId(store.getId());
        item.setName(itemRequest.getName());
        item.setPrice(itemRequest.getPrice());
        item.setDiscount(itemRequest.getDiscount());
        item.setRating(0f);
        item.setDescription(itemRequest.getDescription());
        item.setInStock(itemRequest.getInStock());
        item.setUpdatedAt(Utils.getCurrentMillis());
        item.setCreatedAt(Utils.getCurrentMillis());
        item.setCreatedBy(userForUpdate);
        item.setUpdatedBy(userForUpdate);
        item.setLabel(itemRequest.getLabel());
        item.setCapacity(itemRequest.getCapacity());
        item.setSlug(slug);
        item.setItemCategory(itemRequest.getItemCategory());
        item.setItemSubCategory(itemRequest.getItemSubCategory());
        item.setAvtars(updateStoreImage("", itemRequest.getNewItemImages(), slug, "create"));
        Item result = itemRepository.save(item);
        logger.debug("Exiting createItem");
        return result;
    }


    @Transactional
    public int updateItem(ItemRequest itemRequest, AuthUser loggedUser) {
        logger.info("Entering updateItem with itemRequest: {}, loggedUser: {}", itemRequest, loggedUser);
        Item item = findItemBySlug(itemRequest.getSlug());
        if (item == null) throw new NotFoundException(ResponseMessages.ITEM_NOT_FOUND_1);
        String title = "Item " + item.getName() + " updated.";
        String messageBody = "Item " + item.getName() + " key : " + item.getSlug() + " updated by admin previous data was " +
                item.toString()
                + ". If you have any issue please contact to administrator.";
        sendNotification(title, messageBody, item.getWholesaleId(), loggedUser);
        int result = itemHbRepository.updateItems(itemRequest, loggedUser);
        logger.info("Exiting updateItem");
        return result;
    }


    @Transactional
    public void sendNotification(String title, String messageBody, int storeId, AuthUser loggedUser) {
        logger.debug("Entering sendNotification with title: {}, messageBody: {}, storeId: {}, loggedUser: {}", title, messageBody, storeId, loggedUser);
        StoreNotifications storeNotifications = new StoreNotifications();
        storeNotifications.setTitle(title);
        storeNotifications.setMessageBody(messageBody);
        storeNotifications.setWholesaleId(storeId);
        storeNotifications.setCreatedBy(User.builder().id(loggedUser.getId()).build());
        storeHbRepository.insertStoreNotifications(storeNotifications);
        logger.debug("Exiting sendNotification");
    }

    @Transactional
    public int deleteItem(DeleteRequest deleteRequest, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering deleteItem with deleteRequest: {}, loggedUser: {}", deleteRequest, loggedUser);
        if (!USER_TYPES.SUPER_ADMIN.getType().equals(loggedUser.getUserType()))
            throw new MyException(ResponseMessages.ONLY_SUPERUSER_HAS_PERMISSION_TO_DELETE_IT);
        String slug = deleteRequest.getSlug();
        Item item = findItemBySlug(slug);
        if (item == null) throw new NotFoundException(ResponseMessages.ITEM_NOT_FOUND_TO_DELETE);
        String title = "Item " + item.getName() + " deleted.";
        String messageBody = "Item " + item.getName() + " key : " + item.getSlug() + " deleted by admin. If you have any issue please contact to administrator.";
        sendNotification(title, messageBody, item.getWholesaleId(), loggedUser);
        int result = itemHbRepository.deleteItem(slug);
        logger.debug("Exiting deleteItem");
        return result;
    }


    public int updateStock(String stock, String slug) {
        logger.debug("Entering updateStock with stock: {}, slug: {}", stock, slug);
        if (!Utils.isEmpty(slug)) {
            if (Utils.isEmpty(stock) || !(stock.equals("Y") || stock.equals("N")))
                throw new IllegalArgumentException(ResponseMessages.THE_KEY_STOCK_MUST_BE_Y_OR_N);
            int result = itemHbRepository.updateStock(stock, slug);
            logger.debug("Exiting updateStock with result: {}", result);
            return result;
        }
        throw new IllegalArgumentException(ResponseMessages.THE_KEY_SLUG_CAN_T_BE_BLANK);
    }

    public int updateStatusBySlug(StatusRequest statusRequest, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering updateStatusBySlug with statusRequest: {}, loggedUser: {}", statusRequest, loggedUser);
        // Verify required fields update item status
        Utils.checkRequiredFields(statusRequest, List.of("status", "slug"));
        switch (statusRequest.getStatus()) {
            case "A", "D":
                Item item = findItemBySlug(statusRequest.getSlug());
                if (item == null) return 0;
                String title = "";
                String messageBody = "";
                if (statusRequest.getStatus().equals("D")) {
                    title = "Item " + item.getName() + " deactivated";
                    messageBody = "Item " + item.getName() + " key : " + item.getSlug() + " deactivated by admin because it's legal policy issue. If you have any issue please contact to administrator.";
                } else {
                    title = "Item " + item.getName() + " activated";
                    messageBody = "Item " + item.getName() + " key : " + item.getSlug() + " activated successfully by admin.";
                }
                sendNotification(title, messageBody, item.getWholesaleId(), loggedUser);
                int result = itemHbRepository.updateStatus(statusRequest.getSlug(), statusRequest.getStatus());
                logger.debug("Exiting updateStatusBySlug with result: {}", result);
                return result;
            default:
                throw new IllegalArgumentException(ResponseMessages.STATUS_MUST_BE_A_OR_D);
        }
    }


    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public int insertAllItemsWithExcel(Map<String, List<String>> excel, Integer userId, Integer wholesaleId) {
        logger.debug("Entering insertAllItems with excel: {}, userId: {}, wholesaleId: {}", excel, userId, wholesaleId);
        userId = userId == null ? 0 : userId;
        wholesaleId = wholesaleId == null ? 0 : wholesaleId;
        int result = itemHbRepository.insertItemsList(excel, userId, wholesaleId);
        logger.debug("Exiting insertAllItems with result: {}", result);
        return result;
    }


    public Map<String, Object> getItemDetail(List<String> nameList,
                                             List<String> labelList,
                                             List<String> slugList,
                                             List<String> capacityList,
                                             List<String> priceList,
                                             List<String> discountList,
                                             List<String> inStockList,
                                             int index) {
        Map<String, Object> itemDetailMap = new HashMap<>();
        itemDetailMap.put("NAME", nameList.get(index));
        itemDetailMap.put("LABEL", labelList.get(index));
        itemDetailMap.put("TOKEN", slugList.get(index));
        itemDetailMap.put("CAPACITY", capacityList.get(index));
        itemDetailMap.put("PRICE", priceList.get(index));
        itemDetailMap.put("DISCOUNT", discountList.get(index));
        itemDetailMap.put("IN-STOCK", inStockList.get(index));
        return itemDetailMap;
    }


    @Transactional(rollbackFor = {MyException.class})
    public List<ItemHbRepository.ItemUpdateError> updateItemsWithExcel(Map<String, List<String>> itemsData, Integer userId, Integer wholesaleId) {
        logger.debug("Updating items using excel sheet : {} and userId : {} and wholesaleId : {}", itemsData, userId, wholesaleId);
        List<String> prefix = List.of("N", "O", "Y"); // N=New or No | Y = Yes | O=Old
        ItemHbRepository.ItemUpdateError itemUpdateError = new ItemHbRepository.ItemUpdateError();
        List<ItemHbRepository.ItemUpdateError> errorsList = new ArrayList<>();
        List<String> nameList = itemsData.get("NAME"), labelList = itemsData.get("LABEL"), slugList = itemsData.get("TOKEN"),
                capacityList = itemsData.get("CAPACITY"), priceList = itemsData.get("PRICE"), discountList = itemsData.get("DISCOUNT"), inStockList = itemsData.get("IN-STOCK");

        for (int i = 0; i < nameList.size(); i++) {
            Map<String, Object> itemStringDetail = null;
            try {
                itemStringDetail = getItemDetail(nameList, labelList, slugList, capacityList, priceList, discountList, inStockList, i);
                if (nameList.get(i).trim().isEmpty()) continue; // if there is no item name, leave that row.
                String name = Utils.isValidName(nameList.get(i), "item");
                String label = labelList.get(i);
                String inStock = inStockList.get(i);
                if (!Utils.isEmpty(label)) label = String.valueOf(labelList.get(i).charAt(0)).toUpperCase();
                if (!Utils.isEmpty(inStock)) inStock = String.valueOf(inStockList.get(i).charAt(0)).toUpperCase();
                if (!prefix.contains(label)) throw new MyException(ResponseMessages.LABEL_MUST_BE_NEW_OR_OLD);
                if (!prefix.contains(inStock)) throw new MyException(ResponseMessages.STOCK_MUST_BE_YES_OR_NO);
                Float capacity = capacityList.get(i).isEmpty() ? 0f : Float.parseFloat(capacityList.get(i));
                Float discount = discountList.get(i).isEmpty() ? 0f : Float.parseFloat(discountList.get(i));
                Float price = priceList.get(i).isEmpty() ? 0f : Float.parseFloat(priceList.get(i));
                if (price < discount) throw new MyException(ResponseMessages.PRICE_CAN_T_BE_LESS_THEN_DISCOUNT);

                // creating itemRequest object for update action
                ItemRequest itemRequest = new ItemRequest();
                itemRequest.setName(name);
                itemRequest.setLabel(label);
                itemRequest.setInStock(inStock);
                itemRequest.setCapacity(capacity);
                itemRequest.setPrice(price);
                itemRequest.setDiscount(discount);
                itemRequest.setSlug(slugList.get(i));

                int updated = itemHbRepository.updateExcelSheetItems(itemRequest, userId, wholesaleId);
                if (updated < 1) {
                    itemUpdateError.setItemRowDetail(itemStringDetail);
                    itemUpdateError.setErrorMessage("Item not found.");
                    errorsList.add(itemUpdateError);
                }

            } catch (MyException | IllegalArgumentException e) {
                itemUpdateError.setItemRowDetail(itemStringDetail);
                itemUpdateError.setErrorMessage(e.getMessage());
                errorsList.add(itemUpdateError);
            }
        }
        logger.debug("Exiting updateItemsWithExcel with result: {}", errorsList);
        return errorsList;
    }

    public String updateStoreImage(String previousImages, List<MultipartFile> itemImages, String slug, String action) throws IOException {
        logger.debug("Entering updateStoreImage with previousImages: {}, itemImages: {}, slug: {}, action: {}", previousImages, itemImages, slug, action);
        String newImages = "";
        int index = 0;
        if (itemImages != null) {
            for (MultipartFile multipartFile : itemImages) {
                if (index == itemImages.size() - 1) {
                    newImages += saveItemImageName(multipartFile, slug);
                } else {
                    newImages += saveItemImageName(multipartFile, slug) + ",";
                }
                index += 1;
            }
        }
        String updatedImages = "";
        if (!Utils.isEmpty(previousImages) && !Utils.isEmpty(newImages)) {
            updatedImages = previousImages + newImages;
        } else if (Utils.isEmpty(previousImages)) {
            updatedImages = newImages;
        } else {
            // because it's contained ',' at the end
            updatedImages = previousImages.substring(0, previousImages.length() - 1);
        }
        if (!Utils.isEmpty(updatedImages) && action.equalsIgnoreCase("update")) {
            itemHbRepository.updateItemImage(slug, updatedImages);
        }
        logger.debug("Exiting updateStoreImage with result: {}", updatedImages);
        return updatedImages;
    }


    @Transactional
    public String saveItemImageName(MultipartFile itemImage, String slug) throws IOException {
        logger.debug("Entering saveItemImageName with itemImage: {}, slug: {}", itemImage, slug);
        if (itemImage != null) {
            if (UploadImageValidator.isValidImage(itemImage, GlobalConstant.minWidth,
                    GlobalConstant.minHeight, GlobalConstant.maxWidth, GlobalConstant.maxHeight,
                    GlobalConstant.allowedAspectRatios, GlobalConstant.allowedFormats)) {

                String fileOriginalName = UUID.randomUUID() + itemImage.getOriginalFilename().replaceAll(" ", "_");
                String dirPath = itemImagePath + slug + GlobalConstant.PATH_SEPARATOR;
                File dir = new File(dirPath);
                if (!dir.exists()) dir.mkdirs();
                String filePath = dirPath + fileOriginalName;
                File file = new File(filePath);

                itemImage.transferTo(file);
                //if (!UploadImageValidator.hasWhiteBackground(new File(filePath))) throw new MyException(ResponseMessages.IMAGE_MUST_HAVE_A_WHITE_BACKGROUND);
                logger.debug("Exiting saveItemImageName with result: {}", fileOriginalName);
                return fileOriginalName;
            } else {
                throw new MyException(ResponseMessages.IMAGE_IS_NOT_FIT_IN_ACCEPT_RATIO_PLEASE_RESIZE_YOU_IMAGE_BEFORE_UPLOAD);
            }
        }
        throw new MyException(ResponseMessages.ITEM_IMAGE_CAN_T_BE_NULL_SOMETHING_WENT_WRONG_PLEASE_CONTACT_TO_ADMINISTRATOR);
    }


    public List<CategoryDto> getAllCategory(SearchFilters searchFilters) {
        logger.debug("Entering getAllCategory with searchFilters: {}", searchFilters);
        Sort sort = searchFilters.getOrder().equals("asc") ? Sort.by(searchFilters.getOrderBy()).ascending() : Sort.by(searchFilters.getOrderBy()).descending();
        List<ItemCategory> result = itemCategoryRepository.findAll(sort);
        logger.debug("Exiting getAllCategory with result: {}", result);
        return result.stream().map(categoryMapper::toDto).toList();
    }


    public CategoryDto getItemCategoryDtoById(int categoryId) {
        logger.debug("Entering getItemCategoryDtoById with categoryId: {}", categoryId);
        ItemCategory result = itemCategoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException(ResponseMessages.CATEGORY_NOT_FOUND));
        logger.debug("Exiting getItemCategoryDtoById with result: {}", result);
        return categoryMapper.toDto(result);
    }

    public ItemCategory getItemCategoryById(int categoryId) {
        logger.debug("Entering getItemCategoryById with categoryId: {}", categoryId);
        ItemCategory result = itemCategoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException(ResponseMessages.CATEGORY_NOT_FOUND));
        logger.debug("Exiting getItemCategoryById with result: {}", result);
        return result;
    }

    public int deleteItemCategory(DeleteRequest deleteRequest, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering deleteItemCategory with deleteRequest: {}, loggedUser: {}", deleteRequest, loggedUser);
        // Validating required fields if they are null, this will throw an Exception
        Utils.checkRequiredFields(deleteRequest, List.of("slug"));
        String slug = deleteRequest.getSlug();
        // only super admin can delete it subcategory
        if (!loggedUser.getUserType().equals("SA"))
            throw new PermissionDeniedDataAccessException(ResponseMessages.ONLY_SUPER_ADMIN_CAN_DELETE_ITEM_S_CATEGORY, new Exception());
        Integer categoryId = itemCategoryRepository.getItemCategoryIdBySlug(slug);
        if (categoryId == null) throw new NotFoundException(ResponseMessages.CATEGORY_NOT_FOUND);
        itemHbRepository.switchCategoryToOther(categoryId); // before delete category, assign item to another category.
        int result = itemHbRepository.deleteItemCategory(slug);
        logger.debug("Exiting deleteItemCategory with result: {}", result);
        return result;

    }

    public int deleteItemSubCategory(DeleteRequest deleteRequest, AuthUser loggedUser) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering deleteItemSubCategory with deleteRequest: {}, loggedUser: {}", deleteRequest, loggedUser);
        // Validating required fields if they are null, this will throw an Exception
        Utils.checkRequiredFields(deleteRequest, List.of("slug"));
        String slug = deleteRequest.getSlug();
        // only super admin can delete it subcategory
        if (!loggedUser.getUserType().equals("SA"))
            throw new PermissionDeniedDataAccessException(ResponseMessages.ONLY_SUPER_ADMIN_CAN_DELETE_SUBCATEGORY, new Exception());
        Integer subCategoryId = itemSubCategoryRepository.getItemSubCategoryIdBySlug(slug);
        if (subCategoryId == null) throw new NotFoundException(ResponseMessages.SUBCATEGORY_NOT_FOUND);
        itemHbRepository.switchSubCategoryToOther(subCategoryId); // before delete category assign item to other subcategory.
        int result = itemHbRepository.deleteItemSubCategory(slug);
        logger.debug("Exiting deleteItemSubCategory with result: {}", result);
        return result;

    }


    public List<SubcategoryDto> getAllItemsSubCategories(SubCategoryFilterRequest searchFilters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering getAllItemsSubCategories with searchFilters: {}", searchFilters);
        Sort sort = Sort.by(searchFilters.getOrderBy());
        sort = searchFilters.getOrder().equals("asc") ? sort.ascending() : sort.descending();
        List<SubcategoryDto> result = itemSubCategoryRepository.getSubCategories(searchFilters.getCategoryId(), sort).stream().map(subcategoryMapper::toDto).toList();
        logger.debug("Exiting getAllItemsSubCategories with result: {}", result);
        return result;
    }


    @Transactional(rollbackFor = {MyException.class, RuntimeException.class})
    public CategoryDto saveOrUpdateItemCategory(CategoryRequest categoryRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering saveOrUpdateItemCategory with categoryRequest: {}", categoryRequest);
        // Validate required fields if we found any given field is null, then this will throw Exception
        ItemCategory itemCategory = new ItemCategory();
        if (categoryRequest.getId() != null && categoryRequest.getId() != GlobalConstant.OTHER_CATEGORY) // because we are using 0 for the other category.
            itemCategory.setId(categoryRequest.getId());
        itemCategory.setSlug(UUID.randomUUID().toString());  // slug will also change after during update
        itemCategory.setCategory(categoryRequest.getCategory());
        itemCategory.setIcon(categoryRequest.getIcon());
        ItemCategory result = itemCategoryRepository.save(itemCategory);
        logger.debug("Exiting saveOrUpdateItemCategory with result: {}", result);
        return categoryMapper.toDto(result);
    }

    @Transactional(rollbackFor = {MyException.class, RuntimeException.class})
    public SubcategoryDto saveOrUpdateItemSubCategory(SubCategoryRequest subCategoryRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Entering saveOrUpdateItemSubCategory with subCategoryRequest: {}", subCategoryRequest);
        ItemSubCategory itemSubCategory = new ItemSubCategory();
        if (subCategoryRequest.getId() != null && subCategoryRequest.getId() != GlobalConstant.OTHER_SUBCATEGORY) // because we are using 0 for the other subcategory.
            itemSubCategory.setId(subCategoryRequest.getId());
        itemSubCategory.setSlug(UUID.randomUUID().toString()); // slug will also change after during update
        itemSubCategory.setCategoryId(subCategoryRequest.getCategoryId());
        itemSubCategory.setSubcategory(subCategoryRequest.getSubcategory());
        itemSubCategory.setIcon(subCategoryRequest.getIcon());
        itemSubCategory.setUnit(subCategoryRequest.getUnit());
        itemSubCategory.setUpdatedAt(Utils.getCurrentMillis());
        ItemSubCategory result = itemSubCategoryRepository.save(itemSubCategory);
        logger.debug("Exiting saveOrUpdateItemSubCategory with result: {}", result);
        return subcategoryMapper.toDto(result);
    }


    public List<MeasurementUnit> getAllMeasurementUnit() {
        logger.debug("Entering getAllMeasurementUnit");
        Sort sort = Sort.by("unit").ascending();
        List<MeasurementUnit> result = measurementUnitRepository.findAll(sort);
        logger.debug("Exiting getAllMeasurementUnit with result: {}", result);
        return result;
    }
}
