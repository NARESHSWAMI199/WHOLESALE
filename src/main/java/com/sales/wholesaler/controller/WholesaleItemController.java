package com.sales.wholesaler.controller;

import com.sales.admin.repositories.ItemHbRepository;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import com.sales.global.ResponseMessages;
import com.sales.helpers.ExcelHelper;
import com.sales.request.DeleteRequest;
import com.sales.request.ItemFilterRequest;
import com.sales.requests.ItemRequest;
import com.sales.utils.ReadExcel;
import com.sales.utils.Utils;
import com.sales.utils.WriteExcelUtil;
import com.sales.wholesaler.dto.WholesaleCategoryDto;
import com.sales.wholesaler.dto.WholesaleItemDto;
import com.sales.wholesaler.dto.WholesaleItemListDto;
import com.sales.wholesaler.dto.WholesaleSubcategoryDto;
import com.sales.wholesaler.services.WholesaleItemService;
import com.sales.wholesaler.services.WholesaleStoreService;
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
@RequestMapping(value = {"wholesale/item"})
@RequiredArgsConstructor
@Tag(name = "Wholesale Item Management", description = "APIs for managing items for wholesalers")
public class WholesaleItemController {

    private static final Logger logger = LoggerFactory.getLogger(WholesaleItemController.class);
    private final WriteExcelUtil writeExcel;
    private final WholesaleStoreService wholesaleStoreService;
    private final WholesaleItemService wholesaleItemService;
    private final ReadExcel readExcel;
    @Value("${excel.update.template}")
    String updateItemTemplate;
    @Value("${excel.notUpdated.absolute}")
    String excelNotUpdateItemsFolderPath;

    @PostMapping("/all")
    @PreAuthorize("hasAuthority('wholesale.item.all')")
    @Operation(summary = "Get all items for wholesaler", description = "Retrieves a paginated list of all items associated with the authenticated wholesaler's store based on search filters")
    public ResponseEntity<Page<WholesaleItemListDto>> getAllItem(Authentication authentication, HttpServletRequest request, @RequestBody ItemFilterRequest searchFilters) {
        logger.debug("Starting getAllItem method");
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Integer storeId = wholesaleStoreService.getStoreIdByUserSlug(loggedUser.getId());
        Page<WholesaleItemListDto> alItems = wholesaleItemService.getAllItems(searchFilters, storeId);
        logger.debug("Completed getAllItem method");
        return new ResponseEntity<>(alItems, HttpStatus.OK);
    }

    @GetMapping("/detail/{slug}")
    @PreAuthorize("hasAuthority('wholesale.item.detail')")
    @Operation(summary = "Get item details by slug", description = "Retrieves detailed information for a specific item using its unique slug identifier")
    public ResponseEntity<Map<String, Object>> getItem(@PathVariable String slug) {
        logger.debug("Starting getItem method");
        Map<String, Object> responseObj = new HashMap<>();
        WholesaleItemDto wholesaleItemDto = wholesaleItemService.findItemDtoBySlug(slug);
        if (wholesaleItemDto != null) {
            responseObj.put(ConstantResponseKeys.MESSAGE, ConstantResponseKeys.SUCCESS);
            responseObj.put(ConstantResponseKeys.RES, wholesaleItemDto);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.ITEM_NOT_FOUND);
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        logger.debug("Completed getItem method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(
            example = """
                    {
                      "slug" : "during update",
                      "name": "string",
                      "price": 0,
                      "discount": 0,
                      "description": "string",
                      "label": "string",
                      "capacity": 0,
                      "itemImage": "string",
                      "categoryId": 0,
                      "subCategoryId": 0,
                      "inStock" : "Y|N",
                       "newItemImages": [
                          "image"
                        ]
                    }
                    """
    )))
    @PostMapping(value = {"/add", "/update"})
    @PreAuthorize("hasAnyAuthority('wholesale.item.add','wholesale.item.update','wholesale.item.edit')")
    @Operation(summary = "Add or update item", description = "Creates a new item or updates an existing item for the wholesaler based on the provided item data")
    public ResponseEntity<Map<String, Object>> addOrUpdateItems(Authentication authentication, HttpServletRequest request, @ModelAttribute ItemRequest itemRequest) throws Exception {
        logger.debug("Starting addOrUpdateItems method");
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        String path = request.getRequestURI();
        Map<String, Object> responseObj = wholesaleItemService.createOrUpdateItem(itemRequest, loggedUser, path);
        logger.debug("Completed addOrUpdateItems method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('wholesale.item.delete')")
    @Operation(summary = "Delete item by slug", description = "Deletes an item from the wholesaler's store using the item's slug identifier")
    public ResponseEntity<Map<String, Object>> deleteItemBySlug(Authentication authentication, HttpServletRequest request, @RequestBody DeleteRequest deleteRequest) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting deleteItemBySlug method");
        Map<String, Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isUpdated = wholesaleItemService.deleteItem(deleteRequest, loggedUser);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.ITEM_HAS_BEEN_SUCCESSFULLY_DELETED);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.NO_ITEM_FOUND_TO_DELETE);
            responseObj.put(ConstantResponseKeys.STATUS, 400);
        }
        logger.debug("Completed deleteItemBySlug method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(
            example = """
                    {
                       "slug" : "string (item slug)",
                       "stock" : "Y|N"
                    }
                    """
    )))
    @PostMapping("/stock")
    @PreAuthorize("hasAuthority('wholesale.item.stock.update')")
    @Operation(summary = "Update item stock status", description = "Updates the stock availability status (in stock or out of stock) for a specific item")
    public ResponseEntity<Map<String, Object>> updateItemStock(Authentication authentication, HttpServletRequest request, @RequestBody Map<String, String> params) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Starting updateItemStock method");
        Map<String, Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Integer storeId = wholesaleStoreService.getStoreIdByUserSlug(loggedUser.getId());
        int isUpdated = wholesaleItemService.updateStock(params, storeId);
        if (isUpdated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.ITEM_S_STOCK_HAS_BEEN_SUCCESSFULLY_UPDATED);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
        } else {
            responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.NO_ITEM_FOUND_TO_UPDATE);
            responseObj.put(ConstantResponseKeys.STATUS, 404);
        }
        logger.debug("Completed updateItemStock method");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

    @GetMapping("category")
    @Operation(summary = "Get all item categories", description = "Retrieves a list of all available item categories for wholesale items")
    public ResponseEntity<List<WholesaleCategoryDto>> getAllCategory() {
        logger.debug("Starting getAllCategory method");
        List<WholesaleCategoryDto> itemCategories = wholesaleItemService.getAllCategory();
        logger.debug("Completed getAllCategory method");
        return new ResponseEntity<>(itemCategories, HttpStatus.OK);
    }

    @GetMapping("subcategory/{categoryId}")
    @Operation(summary = "Get subcategories by category ID", description = "Retrieves all subcategories for a specific category ID")
    public ResponseEntity<List<WholesaleSubcategoryDto>> getSubCategory(@PathVariable(required = true) int categoryId) {
        logger.debug("Starting getSubCategory method");
        List<WholesaleSubcategoryDto> itemCategories = wholesaleItemService.getAllItemsSubCategories(categoryId);
        logger.debug("Completed getSubCategory method");
        return new ResponseEntity<>(itemCategories, HttpStatus.OK);
    }

    @PostMapping(value = {"importExcel"})
    @PreAuthorize("hasAuthority('wholesale.item.import')")
    @Operation(summary = "Import items from Excel", description = "Imports and updates items from an uploaded Excel file for the wholesaler")
    public ResponseEntity<Map<String, Object>> importItemsFromExcelSheet(Authentication authentication, HttpServletRequest request, @RequestParam("excelfile") MultipartFile excelSheet) {
        AuthUser user = (SalesUser) authentication.getPrincipal();
        logger.debug("Importing items from Excel sheet for userSlug: {}", user.getSlug());
        Map<String, Object> responseObj = new HashMap<>();
        try {
            if (excelSheet != null && ExcelHelper.hasExcelFormat(excelSheet)) {
                Map<String, List<String>> result = readExcel.getExcelDataInJsonFormat(excelSheet);
                List<ItemHbRepository.ItemUpdateError> updateItemsError = wholesaleItemService.updateItemsWithExcel(result, user.getId());
                if (updateItemsError.isEmpty()) {
                    responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.ITEMS_SUCCESSFULLY_UPDATED);
                    responseObj.put(ConstantResponseKeys.STATUS, 200);
                    logger.debug("Items successfully updated : {} ", updateItemsError);
                } else {
                    // Creating an Excel for which items are not updated
                    String fileName = writeExcel.writeNotUpdatedItemsExcel(updateItemsError, GlobalConstant.HEADERS_NOT_UPDATED_ITEMS_EXCEL, "WHOLESALER_" + user.getSlug());
                    responseObj.put("fileUrl", Utils.getHostUrl(request) + GlobalConstant.ITEMS_NOT_UPDATED_PATH_FOR_WHOLESALE + "WHOLESALER_" + user.getSlug() + "/" + fileName);
                    responseObj.put("message", "Some items are not updated.");
                    responseObj.put("status", 201);
                    logger.debug("Some items are not updated : {} ", updateItemsError);
                }

            } else {
                responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.PLEASE_UPLOAD_A_VALID_EXCEL_FILE_XLS_OR_XLSX);
                responseObj.put(ConstantResponseKeys.STATUS, 400);
            }
        } catch (Exception e) {
            responseObj.put(ConstantResponseKeys.MESSAGE, e.getMessage());
            responseObj.put(ConstantResponseKeys.STATUS, 500);
            logger.error("Facing Exception during updating or importing item from excel sheet  ; {}", e.getMessage());
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

    @PostMapping(value = {"exportExcel"})
    @PreAuthorize("hasAuthority('wholesale.item.export')")
    @Operation(summary = "Export items to Excel", description = "Exports items to an Excel file based on search filters for the wholesaler")
    public ResponseEntity<Object> exportItemsFromExcel(Authentication authentication, @RequestBody ItemFilterRequest searchFilters, HttpServletRequest request) {
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        logger.debug("Exporting items to Excel for user : {}", loggedUser);
        Map<String, Object> responseObj = new HashMap<>();
        try {
            String filePath = wholesaleItemService.createItemsExcelSheet(searchFilters, loggedUser);
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());
            responseObj.put(ConstantResponseKeys.MESSAGE, ResponseMessages.FILE_SUCCESSFULLY_DOWNLOADED);
            responseObj.put(ConstantResponseKeys.STATUS, 200);
            logger.debug("Response during export items excel sheet : {} ", responseObj);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")); // For .xlsx
            headers.setContentDispositionFormData("attachment", "myItemsExcelFile.xlsx");
            return new ResponseEntity<>(resource.getContentAsByteArray(), headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            responseObj.put(ConstantResponseKeys.MESSAGE, e.getMessage());
            responseObj.put(ConstantResponseKeys.STATUS, 500);
            logger.error("Exception during export excel : {}", e.getMessage(), e);
        }
        logger.debug("ENDED exportItemsFromExcel.");
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }

    @GetMapping(value = {"download/update/template"})
    @PreAuthorize("hasAuthority('wholesale.item.template.download')")
    @Operation(summary = "Download Excel update template", description = "Downloads the Excel template file for updating items")
    public ResponseEntity<Object> downloadExcelUpdateTemplate() throws IOException {
        logger.debug("Download excel sheet template for update items");
        Path path = Paths.get(updateItemTemplate);
        Resource resource = new UrlResource(path.toUri());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")); // For .xlsx
        headers.setContentDispositionFormData("attachment", "update_item_template.xlsx");
        return new ResponseEntity<>(resource.getContentAsByteArray(), headers, org.springframework.http.HttpStatus.OK);
    }

    @GetMapping(value = {"notUpdated/{folderName}/{filename}"})
    @PreAuthorize("hasAuthority('wholesale.item.not.updated.download')")
    @Operation(summary = "Download not updated items Excel", description = "Downloads the Excel file containing items that were not updated during import")
    public ResponseEntity<Object> downloadExcelUpdateTemplate(@PathVariable String folderName, @PathVariable String filename) throws IOException {
        Path filePathObj = Paths.get(excelNotUpdateItemsFolderPath);
        Path filePathDynamic = filePathObj.resolve(folderName).normalize();
        Path path = filePathDynamic.resolve(filename).normalize();
        logger.debug("Download excel sheet template for not updated items : {}", path.toAbsolutePath());
        Resource resource = new UrlResource(path.toUri());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")); // For .xlsx
        headers.setContentDispositionFormData("attachment", "update_item_template.xlsx");
        return new ResponseEntity<>(resource.getContentAsByteArray(), headers, org.springframework.http.HttpStatus.OK);
    }

}
