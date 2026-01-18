package com.sales.admin.controllers;


import com.sales.admin.services.StoreReportService;
import com.sales.dto.SearchFilters;
import com.sales.entities.StoreReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/store/report/")
@RequiredArgsConstructor
@Tag(name = "Store Reports", description = "APIs for managing store reports")
public class StoreReportController  {

    private final StoreReportService storeReportService;

    @PreAuthorize("hasAuthority('store.report.all')")
    @PostMapping("all")
    @Operation(summary = "Get all store reports", description = "Retrieves a paginated list of all store reports with optional search filters")
    public ResponseEntity<Page<StoreReport>> findAllItemsReports(@RequestBody SearchFilters searchFilters){
        Page<StoreReport> storeReports = storeReportService.getAllReportByStoreId(searchFilters);
        return new ResponseEntity<>(storeReports, HttpStatusCode.valueOf(200));
    }

}
