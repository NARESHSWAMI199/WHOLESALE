package com.sales.chats.controllers;


import com.sales.chats.services.BlockListService;
import com.sales.chats.services.ChatService;
import com.sales.claims.AuthUser;
import com.sales.entities.Chat;
import com.sales.entities.User;
import com.sales.exceptions.MyException;
import com.sales.global.GlobalConstant;
import com.sales.global.ResponseMessages;
import com.sales.request.MessageDto;
import com.sales.wholesaler.services.WholesaleUserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {


    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final WholesaleUserService wholesaleUserService;
    private final BlockListService blockListService;


    /**
     * @Note : Make sure all @MessageMappings 's prefix is /app/
     */


    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public MessageDto sendMessage(MessageDto message, SimpMessageHeaderAccessor headerAccessor) {
        logger.debug("Sending public message: {}", message);
        // Get the sender's username
        String sender = (String) headerAccessor.getSessionAttributes().get("username");
        // Set the sender in the message
        message.setSender(sender);
        return message;
    }


    /**
     * Here we are using two for receiving and sending chats
     *
     * @sendPrivateMessage : Using for just only text because websocket are faster compare to api
     * @uploadImages : Using api for upload images because we are facing some issue share files or images with websocket
     *
     */

    @MessageMapping("/chat/private/{recipient}")
    public void sendPrivateMessage(@DestinationVariable String recipient, MessageDto message, SimpMessageHeaderAccessor headerAccessor) {
        logger.debug("Sending private message to recipient: {}", recipient);
        AuthUser loggedUser = (User) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("user");
        Chat sentMessage = chatService.sendMessage(message, loggedUser, message.getReceiver());
        if (sentMessage == null) return;
        /* you need to subscribe like  /user/{userId}/queue/private */
        messagingTemplate.convertAndSendToUser(recipient, "/queue/private", sentMessage);
    }


    @MessageMapping("/chat/connect/{slug}")
    public void userConnected(@DestinationVariable String slug, SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        logger.debug("User connected with slug: {}", slug);
        logger.debug("Connected");
        try {
            String sender = Objects.requireNonNull(simpMessageHeaderAccessor.getSessionAttributes()).get("username").toString();
            logger.debug("The sender is : {}", sender);
            User user = wholesaleUserService.findUserBySlug(slug);
            if (user == null) throw new MyException(ResponseMessages.NOT_VALID_USER_TO_CONNECT_FOR_CHAT);
            user.setOnline(true);
            wholesaleUserService.updateLastSeen(user);
            GlobalConstant.onlineUsers.put(slug, user);
        } catch (Exception e) {
            logger.error("Exception during user connection : {}", e.getMessage());
        }

    }

    @MessageMapping("/chat/deactivate/{slug}")
    public void disconnectUser(@DestinationVariable String slug) {
        logger.debug("User disconnected with slug: {}", slug);
        logger.debug("Disconnected");
        try {
            User user = GlobalConstant.onlineUsers.get(slug);
            if (user == null) throw new MyException(ResponseMessages.NOT_VALID_USER_TO_CONNECT_FOR_CHAT);
            user.setOnline(false);
            wholesaleUserService.updateLastSeen(user);
            GlobalConstant.onlineUsers.put(slug, user);
        } catch (Exception e) {
            logger.error("Exception during userDisconnected : {}", e.getMessage());
        }
    }


    @MessageMapping("/chats/was-seen/{recipient}")
    public void isReceiverSeen(@DestinationVariable("recipient") String recipient, SimpMessageHeaderAccessor headerAccessor) {
        User chatLoggedUser = (User) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("user");
        User receiver = wholesaleUserService.findUserBySlug(recipient);
        if (recipient == null) throw new MyException(ResponseMessages.PLEASE_PROVIDE_A_VALID_RECIPIENT);
        logger.debug("Seen Called.....");
        /* Check If you already blocked by receiver or not if blocked, then do nothing eat fivestar */
        boolean isYouBlockedByReceiver = blockListService.isSenderBlockedByReceiver(chatLoggedUser, receiver);
        /* Check you blocked the receiver or not */
        boolean isYouBlockedReceiver = blockListService.isReceiverBlockedBySender(chatLoggedUser, receiver);
        boolean seen = !isYouBlockedByReceiver && !isYouBlockedReceiver;
        logger.debug("Message seen or not :  {} ", seen);
        /* you need to subscribe like  /user/{userId}/queue/private/chat/seen */
        messagingTemplate.convertAndSendToUser(recipient, "/queue/private/chat/seen", seen);
    }


    @MessageMapping("/chat/{slug}/userStatus")
    public void getUserStatus(@DestinationVariable String slug, SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        logger.debug("Checking user status for slug: {}", slug);
        String sender = (String) Objects.requireNonNull(simpMessageHeaderAccessor.getSessionAttributes()).get("username");
        /* you need to subscribe like  /user/{userId}/queue/private/status */
        messagingTemplate.convertAndSendToUser(sender, "/queue/private/status", GlobalConstant.onlineUsers.getOrDefault(slug, new User()));
    }


}
