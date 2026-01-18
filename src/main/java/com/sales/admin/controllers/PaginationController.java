package com.sales.admin.controllers;


import com.sales.admin.services.PaginationService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.UserPaginationDto;
import com.sales.global.ConstantResponseKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/pagination/")
@RequiredArgsConstructor
@Tag(name = "Pagination Settings", description = "APIs for managing user pagination settings")
public class PaginationController  {

    private final PaginationService paginationService;

    @GetMapping("all")
    @PreAuthorize("hasAuthority('pagination.all')")
    @Operation(summary = "Get all user paginations", description = "Retrieves all pagination settings for the logged-in user")
    public ResponseEntity<Map<String,Object>> findAllUserPaginations(Authentication authentication, HttpServletRequest request){
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Map<String,Object> allUserPaginations = paginationService.findUserPaginationsByUserId(loggedUser);
        return new ResponseEntity<>(allUserPaginations, HttpStatus.valueOf(200));
    }


    @PostMapping("update")
    @PreAuthorize("hasAnyAuthority('pagination.update','pagination.edit')")
    @Operation(summary = "Update pagination settings", description = "Updates the pagination row number settings for the user")
    public ResponseEntity<Map<String,Object>> updatePaginationRowNumber(Authentication authentication,HttpServletRequest request, @RequestBody UserPaginationDto userPaginationDto) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Map<String,Object> responseObj = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        userPaginationDto.setUserId(loggedUser.getId());
        int updated = paginationService.updateUserPaginationRowsNumber(userPaginationDto);
        if(updated > 0) {
            responseObj.put(ConstantResponseKeys.MESSAGE,"Pagination updated successfully");
            responseObj.put(ConstantResponseKeys.STATUS,200);
        }else{
            responseObj.put(ConstantResponseKeys.MESSAGE,"No record found to update.");
            responseObj.put(ConstantResponseKeys.STATUS,404);
        }
        return new ResponseEntity<>(responseObj, HttpStatus.valueOf((Integer) responseObj.get(ConstantResponseKeys.STATUS)));
    }




}
