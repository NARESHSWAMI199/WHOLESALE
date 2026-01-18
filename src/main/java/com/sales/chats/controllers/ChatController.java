package com.sales.chats.controllers;

import com.sales.chats.services.ChatService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.dto.MessageDto;
import com.sales.entities.Chat;
import com.sales.entities.User;
import com.sales.exceptions.MyException;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import com.sales.wholesaler.services.WholesaleUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequiredArgsConstructor
@Tag(name = "Chat Management", description = "APIs for managing chat messages and conversations")
public class ChatController  {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final WholesaleUserService wholesaleUserService;



    @PostMapping("/chats/all")
    @Operation(summary = "Get all chats", description = "Retrieves all chat messages for the authenticated user")
    public ResponseEntity<Map<String, List<Chat>>> getALlUsers(Authentication authentication,@RequestBody MessageDto message , HttpServletRequest request){
        logger.debug("Fetching all users for message: {}", message);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        message.setSender(loggedUser.getSlug());
        Map<String, List<Chat>> formatedChatList = chatService.getAllChatBySenderAndReceiverKey(message,request);
        return new ResponseEntity<>(formatedChatList, HttpStatus.valueOf(200));
    }


    @GetMapping("/chats/message/{parentId}")
    public ResponseEntity<Chat> getParentChatMessageByParentId(Authentication authentication,@PathVariable Long parentId , HttpServletRequest request){
        logger.debug("Fetching parent chat using parentId: {}", parentId);
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        Chat parentChat = chatService.getParentMessageById(parentId,loggedUser,request);
        return new ResponseEntity<>(parentChat, HttpStatus.valueOf(200));
    }


    @PostMapping("/chats/parentId")
    public ResponseEntity<Integer> getParentChatMessageBySentTime(@RequestBody MessageDto message , HttpServletRequest request){
        logger.debug("Fetching parent chat using createdAt: {} and sender : {} and receiver : {}", message.getCreatedAt(),message.getSender(),message.getReceiver());
        Integer parentMessageId = chatService.getParentMessageIdByCreatedAt(message,request);
        return new ResponseEntity<>(parentMessageId, HttpStatus.valueOf(200));
    }





    /** Upload images and other files with chat */
    @PostMapping("/chat/upload")
    public ResponseEntity<Map<String,Object>> uploadImages(Authentication authentication,@ModelAttribute MessageDto message ,HttpServletRequest request){
        logger.debug("Uploading images for message: {}", message);
        Map<String,Object> result = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        String recipient = message.getReceiver();
        User receiver = wholesaleUserService.findUserBySlug(recipient);
        if (receiver == null) throw new MyException("Please provide a valid recipient");
        boolean verified = chatService.verifyBeforeSend(loggedUser, recipient);
        if(!verified) return null;

        List<String> allImagesName = chatService.saveAllImages(message, loggedUser);
        if(allImagesName.size() == message.getImages().size()){
            result.put(ConstantResponseKeys.MESSAGE,"All images successfully sent.");
            result.put(ConstantResponseKeys.STATUS , 200);
        }else{
            result.put(ConstantResponseKeys.MESSAGE,"Something went wrong during save images.");
            result.put(ConstantResponseKeys.STATUS , 400);
        }

        /* ------------------------------- sending message and saving message ------------------------------- */
        message = chatService.addImagesList(message, request, allImagesName, loggedUser, recipient);
        chatService.updateMessageToSent(message.getId());
        message.setIsSent("S");
        /*
             You need to subscribe like /user/{userId}/queue/private
             Send a private message to the recipient
         */
        // going to update a message sent successfully
        messagingTemplate.convertAndSendToUser(recipient, "/queue/private",message);

        /*!------------------ message block end ---------------------- */
        return new ResponseEntity<>(result,HttpStatus.valueOf(200));
    }












    @GetMapping("/chat/status/{slug}")
    public ResponseEntity<User> getUserStatus(@PathVariable String slug){
        logger.debug("Getting user status for slug: {}", slug);
        User user = GlobalConstant.onlineUsers.getOrDefault(slug, new User());
        return new ResponseEntity<>(user,HttpStatus.valueOf(200));
    }



    @PostMapping("/chat/seen")
    public ResponseEntity<Map<String,Object>> getUserStatus(Authentication authentication,@RequestBody MessageDto message, HttpServletRequest request){
        logger.debug("Updating seen status for message: {}", message);
        Map<String,Object> result = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        message.setReceiver(loggedUser.getSlug());
        wholesaleUserService.updateSeenMessages(message);
        result.put(ConstantResponseKeys.MESSAGE,"Message successfully updated.");
        result.put(ConstantResponseKeys.STATUS, 200);
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));
    }



    @Value("${chat.get}")
    String filePath;

    @GetMapping("/chat/images/{sender}/{receiver}/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable(required = true) String filename
            , @PathVariable String sender,
    @PathVariable String receiver) throws Exception {
        logger.debug("Fetching file: {} for sender: {} and receiver: {}", filename,sender, receiver);
        Path filePathObj = Paths.get(filePath);
        Path filePathDynamic = filePathObj.resolve(sender+"_"+receiver).normalize();
        Path path = filePathDynamic.resolve(filename).normalize();
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }


    @PostMapping("/chat/delete")
    public ResponseEntity<Map<String,Object>> deleteBySlug(Authentication authentication, @RequestBody MessageDto messageDto, HttpServletRequest request){
        logger.debug("Deleting message: {}", messageDto);
        Map<String,Object> result = new HashMap<>();
        AuthUser loggedUser = (SalesUser) authentication.getPrincipal();
        int isDeleted = chatService.deleteMessage(loggedUser, messageDto);
        if(isDeleted > 0){
            result.put(ConstantResponseKeys.MESSAGE,"deleted successfully");
            result.put(ConstantResponseKeys.STATUS, 200);
        }else{
            result.put(ConstantResponseKeys.MESSAGE,"Something went wrong due to message deleted");
            result.put(ConstantResponseKeys.STATUS, 400);
        }
        if(messageDto.getIsDeleted().equals("B")){
            /* you need to subscribe like  /user/{userId}/queue/private/deleted */
            messagingTemplate.convertAndSendToUser(messageDto.getReceiver(), "/queue/private/deleted",messageDto);
        }
        return new ResponseEntity<>(result,HttpStatus.valueOf((Integer) result.get(ConstantResponseKeys.STATUS)));

    }













}