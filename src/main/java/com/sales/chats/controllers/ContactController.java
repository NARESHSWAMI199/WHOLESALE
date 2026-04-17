package com.sales.chats.controllers;

import com.sales.chats.dto.ContactDto;
import com.sales.chats.dto.UserDto;
import com.sales.chats.services.ContactsService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.entities.User;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.ResponseMessages;
import com.sales.request.ContactRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts Management", description = "APIs for managing user contacts")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    private final ContactsService contactService;

    @GetMapping("all")
    @Operation(summary = "Get all contacts", description = "Retrieves all contacts for the authenticated user")
    public ResponseEntity<List<UserDto>> getAllContactsByUserId(Authentication authentication, HttpServletRequest request) {
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        logger.debug("Fetching all contacts for logged user: {}", loggedUser.getId());
        List<UserDto> allContactsByUserId = contactService.getAllContactsByUserId(loggedUser, request);
        return new ResponseEntity<>(allContactsByUserId, HttpStatus.valueOf(200));
    }

    @PostMapping("add")
    @Operation(summary = "Add new contact", description = "Adds a new contact to the user's contact list")
    public ResponseEntity<Map<String, Object>> addNewContactInContactList(Authentication authentication, @RequestBody ContactRequest contactRequest, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        logger.debug("Adding new contact for logged user: {}", loggedUser.getId());
        ContactDto contact = contactService.addNewContact(loggedUser, contactRequest.getContactSlug());
        if (contact != null) {
            logger.debug("Contact added successfully for user: {}", loggedUser.getId());
            result.put("contact", contact.contactUser());
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.YOUR_CONTACT_HAS_BEEN_SUCCESSFULLY_ADDED);
            result.put(ConstantResponseKeys.STATUS, 200);
        } else {
            logger.error("Failed to add contact for user: {}", loggedUser.getId());
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SOMETHING_WENT_WRONG_DURING_INSERT_YOUR_CONTACT);
            result.put(ConstantResponseKeys.STATUS, 400);
        }
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }

    @PostMapping("remove")
    public ResponseEntity<Map<String, Object>> removeContactAndHisChat(Authentication authentication, @RequestBody ContactRequest contactRequest, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        logger.debug("Removing new contact for logged user: {}", loggedUser.getId());
        int contact = contactService.removeContact(loggedUser, contactRequest.getContactSlug(), contactRequest.getDeleteChats());
        if (contact > 0) {
            logger.debug("Contact removed successfully for user: {}", loggedUser.getId());
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.YOUR_CONTACT_HAS_BEEN_SUCCESSFULLY_REMOVED);
            result.put(ConstantResponseKeys.STATUS, 200);
        } else {
            logger.error("Failed to removed contact for user: {}", loggedUser.getId());
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.NO_CONTACT_FOUND_TO_DELETE);
            result.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }

}
