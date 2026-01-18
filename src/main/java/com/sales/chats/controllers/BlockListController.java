package com.sales.chats.controllers;

import com.sales.chats.services.BlockListService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.entities.BlockedUser;
import com.sales.entities.User;
import com.sales.global.ConstantResponseKeys;
import com.sales.wholesaler.services.WholesaleUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Block List Management", description = "APIs for managing blocked users in chat")
public class BlockListController  {

  
  private static final Logger logger = LoggerFactory.getLogger(BlockListController.class);
    private final BlockListService blockListService;
    private final WholesaleUserService wholesaleUserService;

    @GetMapping("/block/{recipient}")
    @Operation(summary = "Block user", description = "Blocks a user from chatting with the authenticated user")
    public ResponseEntity<Map<String,Object>> addUserInBlockList(Authentication authentication,@PathVariable String recipient, HttpServletRequest request){
        logger.debug("Blocking user: {}", recipient);
        Map<String,Object> result = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        BlockedUser blockedUser = blockListService.addAUserInBlockList(loggedUser, recipient);
        if(blockedUser.getId() > 0){
            result.put(ConstantResponseKeys.MESSAGE,"User has been successfully blocked");
            result.put(ConstantResponseKeys.STATUS,200);
        }else {
            result.put(ConstantResponseKeys.MESSAGE,"Something went wrong during block user");
            result.put(ConstantResponseKeys.STATUS, 400);
        }
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }



    @GetMapping("/unblock/{recipient}")
    public ResponseEntity<Map<String,Object>> removeUserFromBlockList(Authentication authentication,@PathVariable String recipient, HttpServletRequest request){
        logger.debug("Unblocking user: {}", recipient);
        Map<String,Object> result = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        boolean unblocked = blockListService.removeUserFromBlockList(loggedUser.getId(), recipient);
        if(unblocked){
            result.put(ConstantResponseKeys.MESSAGE,"User has been successfully unblocked");
            result.put(ConstantResponseKeys.STATUS,200);
        }else {
            result.put(ConstantResponseKeys.MESSAGE,"Something went wrong during unblock user");
            result.put(ConstantResponseKeys.STATUS, 400);
        }
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }


    @GetMapping("is-blocked/{receiverSlug}")
    public ResponseEntity<Boolean> isReceiverBlocked(Authentication authentication, @PathVariable String receiverSlug, HttpServletRequest request) {
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        User receiver = wholesaleUserService.findUserBySlug(receiverSlug);
        boolean blocked = blockListService.isReceiverBlockedBySender(loggedUser, receiver);
        return new ResponseEntity<>(blocked,HttpStatus.valueOf(200));
    }

}
