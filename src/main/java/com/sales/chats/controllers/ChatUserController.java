package com.sales.chats.controllers;


import com.sales.chats.dto.ChatUserDto;
import com.sales.chats.dto.UserDto;
import com.sales.chats.services.ChatUserService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.entities.User;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.ResponseMessages;
import com.sales.request.ChatUserRequest;
import com.sales.request.ContactRequest;
import com.sales.utils.Utils;
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
@RequestMapping("chat-users")
@RequiredArgsConstructor
@Tag(name = "Chat Users", description = "APIs for managing chat users and contacts")
public class ChatUserController {


    private static final Logger logger = LoggerFactory.getLogger(ChatUserController.class);
    private final ChatUserService chatUserService;

    @GetMapping("all")
    @Operation(summary = "Get all chat users", description = "Retrieves all users available for chat with the authenticated user")
    public ResponseEntity<List<UserDto>> getAllChatUsers(Authentication authentication, HttpServletRequest request) {
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        logger.debug("Fetching all chat users for logged user: {}", loggedUser.getId());
        List<UserDto> allContactsByUserId = chatUserService.getAllChatUsers(loggedUser, request);
        return new ResponseEntity<>(allContactsByUserId, HttpStatus.valueOf(200));
    }


    @GetMapping("is-accepted/{receiver}")
    @Operation(summary = "Check if chat request accepted", description = "Checks if the chat request has been accepted by the logged user")
    public ResponseEntity<String> isChatRequestAcceptedByLoggedUser(Authentication authentication, @PathVariable String receiver, HttpServletRequest request) {
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        logger.debug("Fetching isChatRequestAcceptedByLoggedUser for logged user: {}", loggedUser.getId());
        String accepted = chatUserService.isChatRequestAcceptedByLoggedUser(loggedUser, receiver);
        return new ResponseEntity<>(accepted, HttpStatus.valueOf(200));
    }


    @PostMapping("add")
    public ResponseEntity<Map<String, Object>> addNewChatUser(Authentication authentication, @RequestBody ContactRequest contactRequest, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        logger.debug("Adding new chat user for logged user: {}", loggedUser.getId());
        ChatUserDto chatUser = chatUserService.addNewChatUser(loggedUser, contactRequest.getContactSlug(), "A");
        if (chatUser != null) {
            logger.debug("Chat user added successfully for user: {}", loggedUser.getId());
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.YOUR_CHAT_USER_HAS_BEEN_SUCCESSFULLY_INSERTED);
            result.put(ConstantResponseKeys.STATUS, 200);
        } else {
            logger.error("Failed to add chat user for user: {}", loggedUser.getId());
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SOMETHING_WENT_WRONG_DURING_INSERT_YOUR_CHAT_USER);
            result.put(ConstantResponseKeys.STATUS, 400);
        }
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }


    @PostMapping("remove")
    public ResponseEntity<Map<String, Object>> removeChatUserAndHisChat(Authentication authentication, @RequestBody ContactRequest contactRequest, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        logger.debug("Removing Chat user for logged user: {}", loggedUser.getId());
        int contact = chatUserService.removeChatUser(loggedUser, contactRequest.getContactSlug(), contactRequest.getDeleteChats());
        if (contact > 0) {
            logger.debug("Chat user removed successfully for user: {}", loggedUser.getId());
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.YOUR_CHAT_USER_HAS_BEEN_SUCCESSFULLY_REMOVED);
            result.put(ConstantResponseKeys.STATUS, 200);
        } else {
            logger.error("Failed to removed Chat user for user: {}", loggedUser.getId());
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.NO_CHAT_USER_FOUND_TO_DELETE);
            result.put(ConstantResponseKeys.STATUS, 404);
        }
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }


    @PostMapping("/accept")
    public ResponseEntity<Map<String, Object>> updateChatAcceptStatus(Authentication authentication, HttpServletRequest request, @RequestBody ChatUserRequest chatUserRequest) {
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        logger.debug("Updating chat accept status for user: {}", loggedUser.getId());
        Map<String, Object> result = new HashMap<>();

        boolean accepted = chatUserService.updateAcceptStatus(loggedUser.getId(), chatUserRequest.getReceiverSlug(), chatUserRequest.getStatus());
        String status = "accepted";
        if (!Utils.isEmpty(chatUserRequest.getReceiverSlug()) && chatUserRequest.getReceiverSlug().equals("R")) {
            status = "declined";
        }
        if (accepted) {
            logger.debug("Chat {} successfully for user: {}", status, loggedUser.getId());
            result.put(ConstantResponseKeys.MESSAGE, String.format(ResponseMessages.CHAT_STATUS_CHANGE, status));
            result.put(ConstantResponseKeys.STATUS, 200);
        } else {
            logger.error("Failed to {} chat for user: {}", status, loggedUser.getId());
            result.put(ConstantResponseKeys.MESSAGE, ResponseMessages.SOMETHING_WENT_WRONG_DURING + " " + status + " chat.");
            result.put(ConstantResponseKeys.STATUS, 400);
        }

        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }

}
