package com.sales.chats.controllers;

import com.sales.chats.services.ChatRoomService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.ChatRoomDto;
import com.sales.entities.ChatRoom;
import com.sales.global.ConstantResponseKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("chat_room")
@RequiredArgsConstructor
@Tag(name = "Chat Rooms", description = "APIs for managing chat rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("all")
    @Operation(summary = "Get all chat rooms", description = "Retrieves all available chat rooms")
    public ResponseEntity<List<ChatRoom>> getAllCharRooms() {
        List<ChatRoom> chatRooms = chatRoomService.getAllChatRoom();
        return new ResponseEntity<>(chatRooms, HttpStatusCode.valueOf(200));
    }

    @PostMapping("add")
    @Operation(summary = "Add new chat room", description = "Creates a new chat room")
    public ResponseEntity<Map<String,String>> addNewChatRoom(@RequestBody ChatRoomDto chatRoomDto) {
        Map<String,String> result = new HashMap<>();
        ChatRoom chatRoom = chatRoomService.createRoom(chatRoomDto);
        result.put("roomId",chatRoom.getSlug());
        return new ResponseEntity<>(result,HttpStatus.valueOf(201));
    }

    @PostMapping("update")
    @Operation(summary = "Update chat room", description = "Updates an existing chat room")
    public ResponseEntity<Map<String, Object>> updateChatRoom(Authentication authentication, @RequestBody ChatRoomDto chatRoomDto, HttpServletRequest request) {
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Map<String,Object> result = new HashMap<>();
        int isUpdated = chatRoomService.updateRoom(chatRoomDto, loggedUser);
        if(isUpdated > 0){
            result.put(ConstantResponseKeys.MESSAGE,"Chat room updated successfully");
            result.put(ConstantResponseKeys.STATUS,200);
        }else{
            result.put(ConstantResponseKeys.MESSAGE,"No room found for : "+chatRoomDto.getSlug());
            result.put("status",404);
        }
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get("status")));
    }




}
