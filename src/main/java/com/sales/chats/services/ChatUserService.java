package com.sales.chats.services;

import com.sales.chats.repositories.ChatHbRepository;
import com.sales.chats.repositories.ChatRepository;
import com.sales.chats.repositories.ChatUserHbRepository;
import com.sales.chats.repositories.ChatUserRepository;
import com.sales.claims.AuthUser;
import com.sales.entities.ChatUser;
import com.sales.entities.User;
import com.sales.exceptions.MyException;
import com.sales.exceptions.NotFoundException;
import com.sales.global.GlobalConstant;
import com.sales.global.ResponseMessages;
import com.sales.utils.Utils;
import com.sales.wholesaler.repository.WholesaleUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatUserService {

    private static final Logger logger = LoggerFactory.getLogger(ChatUserService.class);
    private final ChatUserRepository chatUserRepository;
    private final ChatRepository chatRepository;
    private final ChatHbRepository chatHbRepository;
    private final WholesaleUserRepository wholesaleUserRepository;
    private final ChatUserHbRepository chatUserHbRepository;
    private final BlockListService blockListService;

    public List<User> getAllChatUsers(AuthUser loggedUser, HttpServletRequest request) {
        logger.debug("Starting getAllChatUsers method and the user id : {}", loggedUser.getId());
        List<ChatUser> chatUserList = chatUserRepository.getChatUserByUserId(loggedUser.getId()).stream().filter(chatUser -> chatUser.getChatUser() != null).toList();
        List<User> userList = chatUserList.stream().map(ChatUser::getChatUser).toList();

        for (User user : userList) {
            if (user != null) {
                Integer unSeenChatsCount = chatRepository.getUnSeenChatsCount(user.getSlug(), loggedUser.getSlug());
                String hostUrl = Utils.getHostUrl(request);
                user.setAvatarUrl(hostUrl + GlobalConstant.WHOLESALER_IMAGE_PATH + user.getSlug() + GlobalConstant.PATH_SEPARATOR + user.getAvatar());
                user.setChatNotification(unSeenChatsCount);
            }
        }
        logger.debug("Completed getAllChatUsers method");
        return userList;
    }

    public ChatUser addNewChatUser(AuthUser sender, AuthUser receiver, String status) {
        logger.debug("Starting addNewChatUser method with User receiver there the sender is : {} and the receiver is : {} ", sender.getId(), receiver.getId());
        ChatUser userFound = chatUserRepository.findByUserIdAndChatUser(sender.getId(), User.builder().id(receiver.getId()).build());
        if (userFound != null) {
            logger.debug("ChatUser already exists, returning existing user checking in addNewChatUser method");
            return userFound;
        }
        ChatUser chatUser = ChatUser.builder()
                .userId(sender.getId())
                .senderAcceptStatus(status)
                .chatUser(User.builder().id(receiver.getId()).build())
                .build();
        ChatUser savedChatUser = chatUserRepository.save(chatUser); // Create operation
        logger.debug("Completed addNewChatUser method with User receiver");
        return savedChatUser;
    }

    public ChatUser addNewChatUser(AuthUser sender, String receiverSlug, String status) {
        logger.debug("Starting addNewChatUser method with receiverSlug");
        User receiver = wholesaleUserRepository.findUserBySlug(receiverSlug);
        if (receiver == null) {
            logger.error("Receiver not found");
            throw new MyException(ResponseMessages.RECEIVER_NOT_FOUND);
        }
        ChatUser userFound = chatUserRepository.findByUserIdAndChatUser(sender.getId(), receiver);
        if (userFound != null) {
            logger.debug("ChatUser already exists, returning existing user");
            return userFound;
        }

        ChatUser chatUser = ChatUser.builder()
                .userId(sender.getId())
                .chatUser(receiver)
                .senderAcceptStatus(status)
                .build();
        ChatUser savedChatUser = chatUserRepository.save(chatUser); // Create operation
        logger.debug("Completed addNewChatUser method with receiverSlug");
        return savedChatUser;
    }

    public boolean updateAcceptStatus(Integer userId, String receiverSlug, String status) {
        logger.debug("Starting updateAcceptStatus method");
        User receiver = wholesaleUserRepository.findUserBySlug(receiverSlug);
        boolean isUpdated = chatUserHbRepository.updateAcceptStatus(userId, receiver, status); // Update operation
        logger.debug("Completed updateAcceptStatus method");
        return isUpdated;
    }


    public String isChatRequestAcceptedByLoggedUser(AuthUser loggedUser, User receiver) {
        logger.debug("Starting isChatRequestAccepted method with userId : {} and chatUserId : {} ", loggedUser.getId(), receiver.getId());
        ChatUser chatUser = chatUserRepository.findByUserIdAndChatUser(loggedUser.getId(), receiver);
        if (chatUser == null) throw new NotFoundException(ResponseMessages.USER_NOT_FOUND_IN_YOUR_CHAT_USERS_LIST);
        logger.debug("Completed isChatRequestAccepted method");
        return chatUser.getSenderAcceptStatus();
    }


    public String isChatRequestAcceptedByLoggedUser(AuthUser loggedUser, String receiverSlug) {
        logger.debug("Starting isChatRequestAccepted method with userId : {} and chatUser : {} ", loggedUser.getId(), receiverSlug);
        User receiver = wholesaleUserRepository.findUserBySlug(receiverSlug);
        if (receiver == null) throw new NotFoundException(ResponseMessages.RECEIVER_NOT_FOUND);
        ChatUser chatUser = chatUserRepository.findByUserIdAndChatUser(loggedUser.getId(), receiver);
        if (chatUser == null) throw new NotFoundException(ResponseMessages.RECEIVER_NOT_FOUND);
        logger.debug("Completed isChatRequestAcceptedByLoggedUser method");
        return chatUser.getSenderAcceptStatus();
    }


    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public int removeChatUser(AuthUser loggedUser, String chatUserSlug, Boolean deleteChats) {
        logger.debug("Going to remove contact from contact list with loggedUser  {} : and chatUserSlug {} ", loggedUser, chatUserSlug);
        User contactUser = wholesaleUserRepository.findUserBySlug(chatUserSlug);
        if (contactUser == null) throw new NotFoundException(ResponseMessages.NO_CONTACT_USER_FOUND_TO_DELETE);
        Integer deleted = chatUserRepository.deleteChatUserFromChatList(loggedUser.getId(), contactUser);
        if (deleted > 0 && deleteChats) {
            chatHbRepository.deleteChats(loggedUser.getSlug(), chatUserSlug);
        }
        return deleted;
    }


}
