package com.sales.admin.controllers;

import com.sales.admin.repositories.ItemHbRepository;
import com.sales.admin.services.ItemService;
import com.sales.admin.services.StoreService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.*;
import com.sales.entities.*;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import com.sales.utils.ReadExcel;
import com.sales.utils.Utils;
import com.sales.utils.WriteExcelUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping(value = {"admin/item"})
@RequiredArgsConstructor
@Tag(name = "Item Management", description = "APIs for managing items, categories, subcategories, and related operations")
public class ItemController  {


    private final WriteExcelUtil writeExcel;
    private final ItemService itemService;
    private final StoreService storeService;
    private final ReadExcel readExcel;

  private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @PostMapping("/all")
    @PreAuthorize("hasAuthority('item.all')")
    @Operation(summary = "Get all items", description = "Retrieves a paginated list of all items with optional search filters")
    public ResponseEntity<Page<Item>> getAllItem(Authentication authentication, @RequestBody ItemSearchFields searchFilters, HttpServletRequest request) {
        logger.debug("Fetching all items with filters: {}", searchFilters);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Page<Item> alItems = itemService.getAllItems(searchFilters,loggedUser);
        return new ResponseEntity<>(alItems, HttpStatus.OK);
    }

    @GetMapping("/detail/{slug}")
    @PreAuthorize("hasAuthority('item.detail')")
    @Operation(summary = "Get item details", description = "Retrieves detailed information about a specific item by its slug")
    public ResponseEntity<Map<String, Object>> getItem(@PathVariable String slug) {
        logger.debug("Fetching item details for slug: {}", slug);
        Map<String, Object> responseObj = new HashMap<>();
        Item alItems = itemService.findItemBySLug(slug);
        if (alItems != null) {
            responseObj.put(ConstantResponseKeys.MESSAGE, ConstantResponseKeys.SUCCESS);
            responseObj.put(ConstantResponseKeys.RES, alItems);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Item Not Found");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(example = """
                {
                    "slug" : "(only during update) string",
                    "name": "string",
                    "wholesaleSlug": "string",
                    "price": 0,
                    "discount": 0,
                    "rating": 0.0,
                    "description": "string",
                    "capacity": 1,
                    "categoryId": 0,
                    "subCategoryId": 0,
                    "inStock": "Y|N",
                    "label": "N|O",
                    "newItemImages" : "[Multipart images list]",
                    "previousItemImages" , "during update only | string.jpeg,string.png"
                }
            """)
            ))

    @PostMapping(value = {"/add", "/update"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('item.add','item.update','item.edit')")
    @Operation(summary = "Add or update item", description = "Creates a new item or updates an existing item with the provided data and images")
    public ResponseEntity<Map<String, Object>> addOrUpdateItems(Authentication authentication,HttpServletRequest request, @ModelAttribute ItemDto itemDto) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Adding or updating item: {}", itemDto);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        String path = request.getRequestURI();
        logger.error(itemDto.toString());
        Map<String,Object> responseObj = itemService.createOrUpdateItem(itemDto, loggedUser,path);
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @PostMapping(value = {"/importExcel/{wholesaleSlug}"})
    @PreAuthorize("hasAuthority('item.import')")
    @Operation(summary = "Import items from Excel", description = "Imports items from an Excel sheet for a specific wholesale store")
    public ResponseEntity<Object> importItemsFromExcelSheet(Authentication authentication,HttpServletRequest request, @RequestParam("excelfile") MultipartFile excelSheet, @PathVariable(value = "wholesaleSlug") String wholesaleSlug) {
        logger.debug("Importing items from Excel sheet for wholesaleSlug: {}", wholesaleSlug);
        Map<String,Object> responseObj = new HashMap<>();
        try {
            if (excelSheet != null) {
                Map<String,List<String>> result = readExcel.getExcelDataInJsonFormat(excelSheet);
                  AuthUser user = (SalesUser) authentication.getPrincipal();
                Integer wholesaleId = storeService.getStoreIdByStoreSlug(wholesaleSlug);
                if (wholesaleId == null) {
                    logger.error("Wholesale not found.");
                    throw new Exception("Wholesale was not found.");
                }
                List<ItemHbRepository.ItemUpdateError> updateItemsError = itemService.updateItemsWithExcel(result, user.getId(), wholesaleId);
                if(updateItemsError.isEmpty()) {
                    responseObj.put(ConstantResponseKeys.MESSAGE, "Items successfully updated.");
                    responseObj.put(ConstantResponseKeys.STATUS, 200);
                    logger.debug("Items successfully updated : {} ",updateItemsError);
                }else{
                    String fileName = writeExcel.writeNotUpdatedItemsExcel(updateItemsError, GlobalConstant.HEADERS_NOT_UPDATED_ITEMS_EXCEL, wholesaleSlug);
                    responseObj.put("fileUrl", Utils.getHostUrl(request)+GlobalConstant.ITEMS_NOT_UPDATED_PATH_FOR_ADMIN+ wholesaleSlug+
                            GlobalConstant.PATH_SEPARATOR+fileName);
                    responseObj.put(ConstantResponseKeys.MESSAGE, "Some items are not updated.");
                    responseObj.put(ConstantResponseKeys.STATUS, 201);
                    logger.debug("Some items are not updated : {} ",updateItemsError);
                }

            } else {
                responseObj.put(ConstantResponseKeys.MESSAGE, "Please add a proper file.");
                responseObj.put(ConstantResponseKeys.STATUS, 400);
            }
        } catch (Exception e) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Not a valid sheet or something went. Recheck your sheet otherwise contact to administrator.");
            responseObj.put(ConstantResponseKeys.STATUS, 500);
            logger.error("Facing Exception during updating or importing item from excel sheet  ; {}",e.getMessage());
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @PostMapping(value = {"/exportExcel/{wholesaleSlug}","exportExcel"})
    @PreAuthorize("hasAuthority('item.export')")
    @Operation(summary = "Export items to Excel", description = "Exports items to an Excel sheet based on search filters for a specific wholesale or all")
    public ResponseEntity<Object> exportItemsFromExcel(Authentication authentication,@PathVariable(required = false) String wholesaleSlug, @RequestBody ItemSearchFields searchFilters,HttpServletRequest request) {
        logger.debug("Exporting items to Excel for wholesaleSlug: {}", wholesaleSlug);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Map<String,Object> responseObj = new HashMap<>();
        try {
            Store wholesale = storeService.getStoreDetails(wholesaleSlug);
            if (wholesale != null || loggedUser.getUserType().equals("S") || loggedUser.getUserType().equals("SA")) {
                if(wholesale != null) {
                    searchFilters.setStoreId(wholesale.getId());
                }else {
                    logger.debug("Wholesale is not found and wholesaleSlug : {} so that's why we using logged user slug : {} instead wholesale slug ",wholesaleSlug,loggedUser.getSlug());
                    wholesaleSlug = loggedUser.getSlug();
                }
                String filePath = itemService.createItemsExcelSheet(searchFilters,wholesaleSlug,loggedUser);
                Path path = Paths.get(filePath);
                Resource resource = new UrlResource(path.toUri());
                responseObj.put(ConstantResponseKeys.MESSAGE, "File successfully downloaded.");
                responseObj.put(ConstantResponseKeys.STATUS, 200);
                MediaType mediaType = MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                return ResponseEntity.ok().contentType(mediaType).body(resource);
            } else {
                logger.debug("wholeSlug : {}", wholesaleSlug);
                responseObj.put(ConstantResponseKeys.MESSAGE, "Store not exist.");
                responseObj.put(ConstantResponseKeys.STATUS, 500);
            }
        } catch (Exception e) {
            responseObj.put(ConstantResponseKeys.MESSAGE, e.getMessage());
            responseObj.put(ConstantResponseKeys.STATUS, 500);
            logger.error("Exception during export excel : {}",e.getMessage(),e);
        }
        logger.debug("ENDED exportItemsFromExcel.");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('item.delete')")
    @Operation(summary = "Delete item", description = "Deletes an item by its slug")
    public ResponseEntity<Map<String, Object>> deleteItemBySlug(Authentication authentication,HttpServletRequest request,@RequestBody DeleteDto deleteDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Deleting item with slug: {}", deleteDto);
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isUpdated = itemService.deleteItem(deleteDto,loggedUser);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Item has been successfully deleted.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No item found to delete.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }





    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(example = """
            {
               "stock" : "Y|N",
               "slug" : "string"
            }
            """)
    ))
    @PostMapping("/stock")
    @PreAuthorize("hasAuthority('item.stock')")
    @Operation(summary = "Update item stock", description = "Updates the stock status of an item")
    public ResponseEntity<Map<String, Object>> updateItemStock(@RequestBody Map<String, String> params) {
        logger.debug("Updating stock for item with slug: {}", params.get("slug"));
        Map<String,Object> responseObj = new HashMap<>();
        int isUpdated = itemService.updateStock(params.get("stock"), params.get("slug"));
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Item's stock has been successfully updated.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No item found to update.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @PostMapping("/status")
    @PreAuthorize("hasAuthority('item.status')")
    @Operation(summary = "Update item status", description = "Updates the status of an item")
    public ResponseEntity<Map<String, Object>> updateItemStatus(Authentication authentication,HttpServletRequest request ,@RequestBody StatusDto statusDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Updating status for item with statusDto: {}", statusDto);
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isUpdated = itemService.updateStatusBySlug(statusDto,loggedUser);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Item's status has been successfully updated.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No item found to update.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @Value("${item.get}")
    String filePath;

    @GetMapping("/image/{slug}/{filename}")
    @Operation(summary = "Get item image", description = "Retrieves an image file for a specific item")
    public ResponseEntity<Resource> getFile(@PathVariable(required = true) String filename, @PathVariable("slug") String slug ) throws Exception {
        logger.debug("Fetching image file: {} for slug: {}", filename,slug);
        Path filePathObj = Paths.get(filePath);
        Path filePathDynamic = filePathObj.resolve(slug).normalize();
        Path path = filePathDynamic.resolve(filename).normalize();
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }



    // ================= Item category


    @PostMapping("category")
    @PreAuthorize("hasAuthority('item.category')")
    @Operation(summary = "Get all item categories", description = "Retrieves a list of all item categories with optional search filters")
    public ResponseEntity<List<ItemCategory>> getAllCategory(@RequestBody  SearchFilters searchFilters) {
        logger.debug("Fetching all item categories with filters: {}", searchFilters);
        List<ItemCategory> itemCategories = itemService.getAllCategory(searchFilters);
        return new ResponseEntity<>(itemCategories, HttpStatus.OK);
    }

    @PostMapping(value = {"category/add","category/update"})
    @PreAuthorize("hasAnyAuthority('item.category.add','item.category.update','item.category.edit')")
    @Operation(summary = "Add or update item category", description = "Creates or updates an item category")
    public ResponseEntity<Map<String,Object>> saveOrUpdateItemCategory(@RequestBody CategoryDto categoryDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Saving or updating item category: {}", categoryDto);
        Map<String,Object> result = new HashMap<>();
        ItemCategory updatedItemCategory = itemService.saveOrUpdateItemCategory(categoryDto);
        if(updatedItemCategory != null) {
            result.put(ConstantResponseKeys.RES,updatedItemCategory); // during update and inserted for both
            if(categoryDto.getId() != null && categoryDto.getId() != 0) {
                result.put(ConstantResponseKeys.MESSAGE, "Category successfully updated.");
                result.put(ConstantResponseKeys.STATUS, 200);
            }else {
                result.put(ConstantResponseKeys.MESSAGE, "Category successfully inserted.");
                result.put(ConstantResponseKeys.STATUS, 201);
            }
        }
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get("status")));
    }


    @PostMapping("category/delete")
    @PreAuthorize("hasAuthority('item.category.delete')")
    @Operation(summary = "Delete item category", description = "Deletes an item category by ID")
    public ResponseEntity<Map<String,Object>> deleteItemCategoryById(Authentication authentication,HttpServletRequest request,@RequestBody DeleteDto deleteDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Deleting item category with id: {}", deleteDto);
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isUpdated = itemService.deleteItemCategory(deleteDto,loggedUser);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Item's category delete successfully.");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No category to found.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }


    @GetMapping("category/{categoryId}")
    @PreAuthorize("hasAuthority('item.subcategory.detail')")
    @Operation(summary = "Get item category by ID", description = "Retrieves details of a specific item category by its ID")
    public ResponseEntity<ItemCategory> getAllCategory(@PathVariable Integer categoryId) {
        logger.debug("Fetching item category with id: {}", categoryId);
        ItemCategory itemCategories = itemService.getItemCategoryById(categoryId);
        return new ResponseEntity<>(itemCategories, HttpStatus.OK);
    }



    // ================= Item subcategory

    @PostMapping("subcategory")
    @PreAuthorize("hasAnyAuthority('item.subcategory.all','item.subcategory')")
    @Operation(summary = "Get all item subcategories", description = "Retrieves a list of all item subcategories with optional search filters")
    public ResponseEntity<List<ItemSubCategory>> getSubCategory(@RequestBody SearchFilters searchFilters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Fetching all item subcategories with filters: {}", searchFilters);
        List<ItemSubCategory> itemCategories = itemService.getAllItemsSubCategories(searchFilters);
        return new ResponseEntity<>(itemCategories, HttpStatus.OK);
    }

    @PostMapping(value = {"subcategory/add","subcategory/update"})
    @PreAuthorize("hasAnyAuthority('item.subcategory.add','item.subcategory.update','item.subcategory.edit')")
    @Operation(summary = "Add or update item subcategory", description = "Creates or updates an item subcategory")
    public ResponseEntity<Map<String,Object>> saveOrUpdateItemSubCategory(@RequestBody SubCategoryDto subCategoryDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Saving or updating item subcategory: {}", subCategoryDto);
        Map<String,Object> result = new HashMap<>();
        ItemSubCategory updateItemSubCategory = itemService.saveOrUpdateItemSubCategory(subCategoryDto);
        if(updateItemSubCategory != null) {
            result.put(ConstantResponseKeys.RES,updateItemSubCategory); // during update and inserted for both
            if(subCategoryDto.getId() != null) {
                result.put(ConstantResponseKeys.MESSAGE, "Category successfully updated.");
                result.put(ConstantResponseKeys.STATUS, 200);
            }else {
                result.put(ConstantResponseKeys.MESSAGE, "Category successfully inserted.");
                result.put(ConstantResponseKeys.STATUS, 201);
            }
        }
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get("status")));
    }


    @PostMapping("subcategory/delete")
    @PreAuthorize("hasAuthority('item.subcategory.delete')")
    @Operation(summary = "Delete item subcategory", description = "Deletes an item subcategory by ID")
    public ResponseEntity<Map<String,Object>> deleteItemSubCategoryById(Authentication authentication,HttpServletRequest request,@RequestBody DeleteDto deleteDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Deleting item subcategory with id: {}", deleteDto);
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isUpdated = itemService.deleteItemSubCategory(deleteDto,loggedUser);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, "Item's subcategory deleted successfully");
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, "No subcategory found to delete.");
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

    @PreAuthorize("hasAuthority('item.measuring.unit')")
    @GetMapping("units")
    @Operation(summary = "Get all measuring units", description = "Retrieves a list of all available measuring units")
    public ResponseEntity<List<MeasurementUnit>> getALlMeasuringUnitsBySubcategory() {
        logger.debug("Fetching all measuring units by subcategory");
        List<MeasurementUnit> itemMeasurementUnitList = itemService.getAllMeasurementUnit();
        return new ResponseEntity<>(itemMeasurementUnitList, HttpStatus.OK);
    }




    @Value("${excel.update.template}")
    String updateItemTemplate;


    @GetMapping(value = {"download/update/template"})
    @Operation(summary = "Download update template", description = "Downloads the Excel template for updating items")
    public ResponseEntity<Object> downloadExcelUpdateTemplate() throws IOException {
        logger.debug("Download excel sheet template for update items" );
        Path path = Paths.get(updateItemTemplate);
        Resource resource = new UrlResource(path.toUri());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")); // For .xlsx
        headers.setContentDispositionFormData("attachment", "update_item_template.xlsx");
        return new ResponseEntity<>(resource.getContentAsByteArray(), headers, org.springframework.http.HttpStatus.OK);
    }



    @Value("${excel.notUpdated.absolute}")
    String excelNotUpdateItemsFolderPath;


    @GetMapping(value = {"notUpdated/{wholesaleSlug}/{filename}"})
    @Operation(summary = "Download not updated items Excel", description = "Downloads the Excel file containing items that were not updated during import")
    public ResponseEntity<Object> downloadExcelUpdateTemplate(@PathVariable String wholesaleSlug ,@PathVariable String filename) throws IOException {
        logger.debug("Download excel sheet template for not updated items : {}  : {}",excelNotUpdateItemsFolderPath,filename);
        Path filePathObj = Paths.get(excelNotUpdateItemsFolderPath);
        Path filePathDynamic = filePathObj.resolve(wholesaleSlug).normalize();
        Path path = filePathDynamic.resolve(filename).normalize();
        Resource resource = new UrlResource(path.toUri());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")); // For .xlsx
        headers.setContentDispositionFormData("attachment", "update_item_template.xlsx");
        return new ResponseEntity<>(resource.getContentAsByteArray(), headers, org.springframework.http.HttpStatus.OK);
    }






}
