package com.sales.wholesaler.services;


import com.google.gson.Gson;
import com.sales.admin.repositories.ItemHbRepository;
import com.sales.claims.AuthUser;
import com.sales.dto.DeleteDto;
import com.sales.dto.GraphDto;
import com.sales.dto.ItemSearchFields;
import com.sales.entities.Item;
import com.sales.entities.ItemCategory;
import com.sales.entities.ItemSubCategory;
import com.sales.entities.User;
import com.sales.exceptions.MyException;
import com.sales.exceptions.NotFoundException;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import com.sales.requests.ItemRequest;
import com.sales.utils.DateUtils;
import com.sales.utils.UploadImageValidator;
import com.sales.utils.Utils;
import com.sales.utils.WriteExcelUtil;
import com.sales.wholesaler.dto.WholesaleCategoryDto;
import com.sales.wholesaler.dto.WholesaleItemDto;
import com.sales.wholesaler.dto.WholesaleItemListDto;
import com.sales.wholesaler.dto.WholesaleSubcategoryDto;
import com.sales.wholesaler.mapper.WholesaleCategoryMapper;
import com.sales.wholesaler.mapper.WholesaleItemMapper;
import com.sales.wholesaler.mapper.WholesaleSubcategoryMapper;
import com.sales.wholesaler.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

import static com.sales.specifications.ItemsSpecifications.*;

@Service
@RequiredArgsConstructor
public class WholesaleItemService  {

    
    private static final Logger logger = LoggerFactory.getLogger(WholesaleItemService.class);
    private final WholesaleItemRepository wholesaleItemRepository;
    private final WholesaleItemHbRepository wholesaleItemHbRepository;
    private final WholesaleItemSubCategoryRepository wholesaleItemSubCategoryRepository;
    private final WholesaleItemCategoryRepository wholesaleItemCategoryRepository;
    private final WholesaleStoreRepository wholesaleStoreRepository;
    private final WriteExcelUtil writeExcel;
    private final WholesaleItemMapper wholesaleItemMapper;
    private final WholesaleCategoryMapper wholesaleCategoryMapper;
    private final WholesaleSubcategoryMapper wholesaleSubcategoryMapper;

    @Value("${item.absolute}")
    String itemImagePath;


    public Page<WholesaleItemListDto> getAllItems(ItemSearchFields searchFilters, Integer storeId) {
        logger.debug("Starting getAllItems method with searchFilters: {}, storeId: {}", searchFilters, storeId);
        Sort sort = searchFilters.getOrder().equalsIgnoreCase("asc") ?
                Sort.by(searchFilters.getOrderBy()).ascending() :
                Sort.by(searchFilters.getOrderBy()).descending();
        Specification<Item> specification = Specification.allOf(
                (containsName(searchFilters.getSearchKey().trim())
                        .or(hasSlug(searchFilters.getSearchKey())))
                        .and(isWholesale(storeId))
                        .and(isStatus(searchFilters.getStatus()))
                        .and(inStock(searchFilters.getInStock()))
                        .and(isLabel(searchFilters.getLabel()))
                        .and(greaterThanOrEqualFromDate(searchFilters.getFromDate()))
                        .and(lessThanOrEqualToToDate(searchFilters.getToDate()))
        );
        Pageable pageable = PageRequest.of(searchFilters.getPageNumber(), searchFilters.getSize(), sort);
        Page<WholesaleItemListDto> items = wholesaleItemHbRepository.findAll(specification, pageable);
        logger.debug("Completed getAllItems method");
        return items;
    }




    public Map<String, Integer> getItemCounts (Integer storeId) {
        logger.debug("Starting getItemCounts method with storeId: {}", storeId);
        Map<String,Integer> responseObj = new HashMap<>();
        responseObj.put("all",wholesaleItemRepository.totalItemCount(storeId));
        responseObj.put("inStock",wholesaleItemRepository.getItemCountInStock("Y",storeId));
        responseObj.put("outStock",wholesaleItemRepository.getItemCountInStock("N",storeId));
        responseObj.put("active",wholesaleItemRepository.optionItemCount("A",storeId));
        responseObj.put("deactive",wholesaleItemRepository.optionItemCount("D",storeId));
        logger.debug("Completed getItemCounts method");
        return responseObj;
    }

    public Map<String, Integer> getItemCountsForNewLabel (Integer storeId) {
        logger.debug("Starting getItemCountsForNewLabel method with storeId: {}", storeId);
        Map<String,Integer> responseObj = new HashMap<>();
        responseObj.put("all",wholesaleItemRepository.getItemCountLabel("N",storeId));
        responseObj.put("inStock",wholesaleItemRepository.getItemCountInStock("Y","N",storeId));
        responseObj.put("outStock",wholesaleItemRepository.getItemCountInStock("N","N",storeId));
        responseObj.put("active",wholesaleItemRepository.optionItemCountLabel("N","A",storeId));
        responseObj.put("deactive",wholesaleItemRepository.optionItemCountLabel("N","D",storeId));
        logger.debug("Completed getItemCountsForNewLabel method");
        return responseObj;
    }

    public Map<String, Integer> getItemCountsForOldLabel (Integer storeId) {
        logger.debug("Starting getItemCountsForOldLabel method with storeId: {}", storeId);
        Map<String,Integer> responseObj = new HashMap<>();
        responseObj.put("all",wholesaleItemRepository.getItemCountLabel("O",storeId));
        responseObj.put("inStock",wholesaleItemRepository.getItemCountInStock("Y","O",storeId));
        responseObj.put("outStock",wholesaleItemRepository.getItemCountInStock("N","O",storeId));
        responseObj.put("active",wholesaleItemRepository.optionItemCountLabel("O","A",storeId));
        responseObj.put("deactive",wholesaleItemRepository.optionItemCountLabel("O","D",storeId));
        logger.debug("Completed getItemCountsForOldLabel method");
        return responseObj;
    }

    public Map<String, Integer> getItemCountsForInStock (Integer storeId) {
        logger.debug("Starting getItemCountsForInStock method with storeId: {}", storeId);
        Map<String,Integer> responseObj = new HashMap<>();
        responseObj.put("all",wholesaleItemRepository.getItemCountInStock("Y",storeId));
        responseObj.put("active",wholesaleItemRepository.optionItemCountInStock("Y","A",storeId));
        responseObj.put("deactive",wholesaleItemRepository.optionItemCountInStock("Y","D",storeId));
        logger.debug("Completed getItemCountsForInStock method");
        return responseObj;
    }


    public Map<String, Integer> getItemCountsForOutStock (Integer storeId) {
        logger.debug("Starting getItemCountsForOutStock method with storeId: {}", storeId);
        Map<String,Integer> responseObj = new HashMap<>();
        responseObj.put("all",wholesaleItemRepository.getItemCountInStock("N",storeId));
        responseObj.put("active",wholesaleItemRepository.optionItemCountLabel("N","A",storeId));
        responseObj.put("deactive",wholesaleItemRepository.optionItemCountLabel("N","D",storeId));
        logger.debug("Completed getItemCountsForOutStock method");
        return responseObj;
    }



    @Transactional
    public WholesaleItemDto findItemBySLug(String slug) {
        logger.debug("Starting findItemBySLug method with slug: {}", slug);
        Item item = wholesaleItemRepository.findItemBySlug(slug);
        logger.debug("Completed findItemBySLug method");
        return wholesaleItemMapper.toDto(item) ;
    }


    public String getItemStatus(String slug) {
        logger.debug("Starting getItemStatus method with slug: {}", slug);
        String status = wholesaleItemRepository.getItemStatus(slug);
        logger.debug("Completed getItemStatus method");
        return status;
    }

    public void validateRequiredFields(ItemRequest itemRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting validateRequiredFields method with itemRequest: {}", itemRequest);
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(itemRequest,List.of(
                "name",
                "price",
                "discount",
                "description",
//                "capacity",
                "categoryId",
                "subCategoryId"
        ));
        logger.debug("Completed validateRequiredFields method");
    }

    public void validateRequiredFieldsBeforeCreateItem(ItemRequest itemRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting validateRequiredFieldsBeforeCreateItem method with itemRequest: {}", itemRequest);
        /** @Note : During creation we are checking only extra required params  */
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(itemRequest,List.of(
                "rating",
                "inStock",
                "label",
                "newItemImages"
        ));
        logger.debug("Completed validateRequiredFieldsBeforeCreateItem method");
    }



    @Transactional(rollbackOn = {IllegalArgumentException.class,MyException.class, RuntimeException.class,Exception.class})
    public Map<String, Object> createOrUpdateItem(ItemRequest itemRequest, AuthUser loggedUser, String path) throws Exception {
        logger.debug("Starting createOrUpdateItem method with itemRequest: {}, loggedUser: {}, path: {}", itemRequest, loggedUser, path);
        // if there is any required field null then this will throw IllegalArgumentException
        validateRequiredFields(itemRequest);

        // Discount can't be less than item's price
        if(itemRequest.getPrice() < itemRequest.getDiscount()) throw new IllegalArgumentException("Discount can't be greater then price.");
        // If item name not in proper syntax this will throw Exception
        String itemName = Utils.isValidName( itemRequest.getName(),"item");
        itemRequest.setName(itemName);

        Integer storeId = wholesaleStoreRepository.getStoreIdByUserId(loggedUser.getId());
        itemRequest.setStoreId(storeId);

        // Getting category and subcategory from database behalf on provided Ids.
        ItemCategory itemCategory = wholesaleItemCategoryRepository.findById(itemRequest.getCategoryId()).orElseThrow(() -> new NotFoundException("Store item category not found."));
        if(itemCategory == null) throw new IllegalArgumentException("Invalid categoryId.");
        itemRequest.setItemCategory(itemCategory);
        ItemSubCategory itemSubCategory = wholesaleItemSubCategoryRepository.findById(itemRequest.getSubCategoryId()).orElseThrow(() -> new NotFoundException("Store item subcategory not found."));
        if(itemSubCategory == null) throw new IllegalArgumentException("Invalid subCategoryId.");
        itemRequest.setItemSubCategory(itemSubCategory);

        Map<String, Object> responseObj = new HashMap<>();

        // Going to update Item
        if (!Utils.isEmpty(itemRequest.getSlug()) || path.contains("update")) {

            // if there is any required field null then this will throw IllegalArgumentException
            Utils.checkRequiredFields(itemRequest,List.of("slug"));

            // Getting item's status from database and validating the item not blocked
            String itemStatus = getItemStatus(itemRequest.getSlug());
            if(itemStatus == null) throw new NotFoundException("No item found to update.");
            if (itemStatus.equals("D")) throw new MyException("You can't update a blocked item.");

            // Update item images
            updateStoreImage(itemRequest.getPreviousItemImages(), itemRequest.getNewItemImages(), itemRequest.getSlug(),"update");
            int isUpdated = updateItem(itemRequest, loggedUser); // Update operation
            if (isUpdated > 0) {
                responseObj.put(ConstantResponseKeys.MESSAGE, "successfully updated.");
                responseObj.put(ConstantResponseKeys.STATUS, 200);
            } else {
                responseObj.put(ConstantResponseKeys.MESSAGE, "No item found to update.");
                responseObj.put(ConstantResponseKeys.STATUS, 404);
            }
        } else {  // Going to crate Item
            // if there is any required field null then this will throw IllegalArgumentException
            validateRequiredFieldsBeforeCreateItem(itemRequest);

            Item createdItem = createItem(itemRequest, loggedUser); // Create operation
            responseObj.put(ConstantResponseKeys.RES, createdItem);
            responseObj.put(ConstantResponseKeys.MESSAGE, "Successfully inserted.");
            responseObj.put(ConstantResponseKeys.STATUS, 201);
        }
        logger.debug("Completed createOrUpdateItem method");
        return responseObj;
    }

    @Transactional(rollbackOn = {IllegalArgumentException.class,MyException.class, RuntimeException.class,Exception.class})
    public Item createItem (ItemRequest itemRequest, AuthUser loggedUser) throws MyException, IOException {
        logger.debug("Starting createItem method with itemRequest: {}, loggedUser: {}", itemRequest, loggedUser);
        User userForUpdate = User.builder()
                .id(loggedUser.getId())
                .build();
        String slug = UUID.randomUUID().toString();
        Item item = new Item();
        item.setWholesaleId(itemRequest.getStoreId());
        item.setName(itemRequest.getName());
        item.setPrice(itemRequest.getPrice());
        item.setDiscount(itemRequest.getDiscount());
        item.setRating(itemRequest.getRating());
        item.setDescription(itemRequest.getDescription());
        item.setInStock(itemRequest.getInStock());
        item.setUpdatedAt(Utils.getCurrentMillis());
        item.setCreatedAt(Utils.getCurrentMillis());
        item.setCreatedBy(userForUpdate);
        item.setUpdatedBy(userForUpdate);
        item.setLabel(itemRequest.getLabel());
        item.setCapacity(itemRequest.getCapacity());
        item.setItemCategory(itemRequest.getItemCategory());
        item.setItemSubCategory(itemRequest.getItemSubCategory());
        item.setSlug(slug);
        item.setAvtars(updateStoreImage("", itemRequest.getNewItemImages(),slug,"create"));
        Item savedItem = wholesaleItemRepository.save(item); // Create operation
        logger.debug("Completed createItem method");
        return savedItem;
    }

    public String updateStoreImage(String previousImages, List<MultipartFile> itemImages,String slug,String action) throws IOException {
        logger.debug("Starting updateStoreImage method with previousImages: {}, itemImages: {}, slug: {}, action: {}", previousImages, itemImages, slug, action);
        String newImages = "";
        int index = 0;
        if(itemImages != null) {
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
        if(!Utils.isEmpty(previousImages) && !Utils.isEmpty(newImages)) {
            updatedImages =  previousImages+newImages;
        }else if(Utils.isEmpty(previousImages)){
            updatedImages = newImages;
        }else {
            /** because it's contains ',' at the end */
            updatedImages = previousImages.substring(0,previousImages.length()-1);
        }
        if(!Utils.isEmpty(updatedImages) && action.equalsIgnoreCase("update")){
            wholesaleItemHbRepository.updateItemImages(slug, updatedImages); // Update operation
        }
        logger.debug("Completed updateStoreImage method");
        return updatedImages;
    }


    @Transactional
    public String saveItemImageName(MultipartFile itemImage, String slug) throws IOException {
        logger.debug("Starting saveItemImageName method with itemImage: {}, slug: {}", itemImage, slug);
        if(itemImage !=null) {
            if (UploadImageValidator.isValidImage(itemImage, GlobalConstant.minWidth,
                    GlobalConstant.minHeight, GlobalConstant.maxWidth, GlobalConstant.maxHeight,
                    GlobalConstant.allowedAspectRatios, GlobalConstant.allowedFormats)) {

                String fileOriginalName = UUID.randomUUID()+itemImage.getOriginalFilename().replaceAll(" ", "_");
                String dirPath = itemImagePath+slug+GlobalConstant.PATH_SEPARATOR;
                File dir = new File(dirPath);
                if(!dir.exists()) dir.mkdirs();
                String filePath = dirPath+fileOriginalName;
                File file = new File(filePath);
                itemImage.transferTo(file);
                //if (!UploadImageValidator.hasWhiteBackground(new File(filePath))) throw new MyException("Image must have a white background");
                logger.debug("Completed saveItemImageName method");
                return fileOriginalName;
            } else {
                throw new MyException("Image is not fit in accept ratio. please resize you image before upload.");
            }
        }
        throw new MyException("Something went wrong.Please contact to administrator");
    }




    public int updateItem(ItemRequest itemRequest, AuthUser loggedUser) {
        logger.debug("Starting updateItem method with itemRequest: {}, loggedUser: {}", itemRequest, loggedUser);
        int updateCount = wholesaleItemHbRepository.updateItems(itemRequest,loggedUser); // Update operation
        logger.debug("Completed updateItem method");
        return updateCount;
    }

    public int deleteItem(DeleteDto deleteDto, Integer storeId) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting deleteItem method with deleteDto: {}, storeId: {}", deleteDto, storeId);
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(deleteDto,List.of("slug"));
        String slug = deleteDto.getSlug();
        String status = getItemStatus(slug);
        if(status == null) throw new NotFoundException("No item to delete.");
        if (status.equals("D")) throw new IllegalArgumentException("Can't deactivated items.");
        int deleteCount = wholesaleItemHbRepository.deleteItem(slug,storeId); // Update operation
        logger.debug("Completed deleteItem method");
        return deleteCount;
    }

    public int updateStock(Map<String,String> params,Integer storeId) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateStock method with params: {}, storeId: {}", params, storeId);
        // if there is any required field null then this will throw IllegalArgumentException
        Utils.checkRequiredFields(params, List.of("slug", "stock"));
        String slug = params.get("slug");
        String stock = params.get("stock");
        int updateCount = wholesaleItemHbRepository.updateStock(stock,slug,storeId); // Update operation
        logger.debug("Completed updateStock method");
        return updateCount;
    }



    public Map<String,Object> getItemCountByMonths(GraphDto graphDto,Integer storeId){
        logger.debug("Starting getItemCountByMonths method with graphDto: {}, storeId: {}", graphDto, storeId);
        Date date = new Date();
        int currentYear = date.getYear();
        List<Integer> months = graphDto.getMonths();
        months = (months == null || months.isEmpty()) ?
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12) : months;
        int year = Objects.nonNull(graphDto.getYear()) ? graphDto.getYear() : currentYear;

        Map<String,Object> monthsObj= new LinkedHashMap<>();
        for(Integer month : months) {
            long start = DateUtils.getStartOfMonthEpochMillis(year, month);
            long end = DateUtils.getStartOfNextMonthEpochMillis(year, month);
            Integer total = wholesaleItemRepository.totalItemsViaMonth(storeId, start, end);
            monthsObj.put(getMonthName(month),total);
        }
        logger.debug("Completed getItemCountByMonths method");
        return monthsObj;
    }


    public String getMonthName(int month) {
        logger.debug("Starting getMonthName method with month: {}", month);
        if (month <= 0 || month > 12) {
            return null;
        }
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("eng"));
        logger.debug("Completed getMonthName method");
        return monthName;
    }

    public List<WholesaleCategoryDto> getAllCategory() {
        logger.debug("Starting getAllCategory method");
        Sort sort = Sort.by("category").ascending();
        List<WholesaleCategoryDto> categories = wholesaleItemCategoryRepository.findAll(sort).stream().map(wholesaleCategoryMapper::toDto).toList();
        logger.debug("Completed getAllCategory method");
        return categories;
    }


    public List<WholesaleSubcategoryDto> getAllItemsSubCategories(int categoryId) {
        logger.debug("Starting getAllItemsSubCategories method with categoryId: {}", categoryId);
        List<WholesaleSubcategoryDto> subCategories = wholesaleItemSubCategoryRepository.getSubCategories(categoryId).stream().map(wholesaleSubcategoryMapper::toDto).toList();
        logger.debug("Completed getAllItemsSubCategories method");
        return subCategories;
    }




    public String createItemsExcelSheet(ItemSearchFields searchFilters,AuthUser loggedUser) throws IOException {
        logger.debug("Entering createItemsExcelSheet with searchFilters: {}", searchFilters);
        int wholesaleId = searchFilters.getStoreId();
        Specification<Item> specification = Specification.allOf(
                (containsName(searchFilters.getSearchKey().trim())
                        .or(hasSlug(searchFilters.getSearchKey())))
                        .and(isWholesale(wholesaleId))
                        .and(isStatus(searchFilters.getStatus()))
                        .and(inStock(searchFilters.getInStock()))
                        .and(isLabel(searchFilters.getLabel()))
                        .and(greaterThanOrEqualFromDate(searchFilters.getFromDate()))
                        .and(lessThanOrEqualToToDate(searchFilters.getToDate()))
        );
        List<Item> itemsList = wholesaleItemRepository.findAll(specification);
        Map<String,List<Object>> result = new HashMap<>();
        for (Item item : itemsList){
            String items = new Gson().toJson(item);
            Map<String,Object> itemMap = new Gson().fromJson(items,Map.class);
            itemMap.forEach((key,value)->{
                if(key.equals("wholesale")){
                    // skip...
                }
                else if (result.containsKey(key.toUpperCase())){
                    result.get(key.toUpperCase()).add(itemMap.get(key));
                }else {
                    List<Object> valueList = new ArrayList<>();
                    valueList.add(value);
                    result.put(key.toUpperCase(),valueList);
                }
            });
        }
        int totalItem = itemsList.size();
        String filePath = writeExcel.createExcelSheet(result, totalItem,GlobalConstant.HEADERS_FOR_ITEMS,"WHOLESALER_"+loggedUser.getSlug());
        logger.debug("Exiting createItemsExcelSheet");
        return filePath;
    }


    public Map<String, Object> getItemString(List<String> nameList,
         List<String> labelList,
         List<String> slugList,
         List<String> capacityList,
         List<String> priceList,
         List<String> discountList,
         List<String> inStockList,
         int index) {
        Map<String,Object> itemDetailMap = new HashMap<>();
        itemDetailMap.put("NAME",nameList.get(index));
        itemDetailMap.put("LABEL",labelList.get(index));
        itemDetailMap.put("TOKEN",slugList.get(index));
        itemDetailMap.put("CAPACITY",capacityList.get(index));
        itemDetailMap.put("PRICE",priceList.get(index));
        itemDetailMap.put("DISCOUNT",discountList.get(index));
        itemDetailMap.put("STOCK",inStockList.get(index));
        return itemDetailMap;
    }


    @Transactional(rollbackOn = {MyException.class})
    public List<ItemHbRepository.ItemUpdateError> updateItemsWithExcel(Map<String,List<String>> itemsData, Integer userId){
        Integer wholesaleId = wholesaleStoreRepository.getStoreIdByUserId(userId);
        logger.debug("Updating items using excel sheet : {} and userId : {} and wholesaleId : {}",itemsData,userId,wholesaleId);
        List<String> prefix = List.of("N","O","Y"); // N=New or No | Y = Yes | O=Old
        ItemHbRepository.ItemUpdateError itemUpdateError = new ItemHbRepository.ItemUpdateError();
        List<ItemHbRepository.ItemUpdateError> errorsList = new ArrayList<>();
        List<String> nameList = itemsData.get("NAME") , labelList = itemsData.get("LABEL"),slugList = itemsData.get("TOKEN"),
                capacityList = itemsData.get("CAPACITY"),priceList = itemsData.get("PRICE"),discountList = itemsData.get("DISCOUNT")
                ,inStockList = itemsData.get("IN-STOCK");
        if(Objects.isNull(nameList) ||
            Objects.isNull(labelList) ||
            Objects.isNull(slugList) ||
            Objects.isNull(capacityList) ||
            Objects.isNull(priceList) ||
            Objects.isNull(discountList) ||
            Objects.isNull(inStockList)
        ) throw new MyException("Excel sheet columns missing.");

        for (int i = 0; i < nameList.size(); i++) {
            Map<String,Object> itemStringDetail = null;
            try {
                itemStringDetail = getItemString(nameList,labelList,slugList,capacityList,priceList,discountList,inStockList,i);
                if(nameList.get(i).trim().isEmpty()) continue; // if there is no item name, leave that row.
                String name = Utils.isValidName(nameList.get(i),"item");
                String label = labelList.get(i);
                String inStock = inStockList.get(i);
                if(!Utils.isEmpty(label)) label = String.valueOf(labelList.get(i).charAt(0)).toUpperCase();
                if(!Utils.isEmpty(inStock)) inStock = String.valueOf(inStockList.get(i).charAt(0)).toUpperCase();
                if(!prefix.contains(label)) throw new MyException("Label must be New or Old.");
                if(!prefix.contains(inStock)) throw new MyException("Stock must be Yes or NO.");
                Float capacity = capacityList.get(i).isEmpty() ? 0f : Float.parseFloat(capacityList.get(i));
                Float discount = discountList.get(i).isEmpty() ? 0f : Float.parseFloat(discountList.get(i));
                Float price = priceList.get(i).isEmpty() ? 0f : Float.parseFloat(priceList.get(i));
                if (price < discount) throw new MyException("Price can't be less then discount.");

                // creating itemRequest object for update action
                ItemRequest itemRequest = new ItemRequest();
                itemRequest.setName(name);
                itemRequest.setLabel(label);
                itemRequest.setInStock(inStock);
                itemRequest.setCapacity(capacity);
                itemRequest.setPrice(price);
                itemRequest.setDiscount(discount);
                itemRequest.setSlug(slugList.get(i));

                int updated = wholesaleItemHbRepository.updateExcelSheetItems(itemRequest,userId,wholesaleId);
                if(updated < 1){
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





}
