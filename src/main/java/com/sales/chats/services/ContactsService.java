package com.sales.chats.services;

import com.sales.chats.dto.ContactDto;
import com.sales.chats.dto.UserDto;
import com.sales.chats.mapper.ContactMapper;
import com.sales.chats.mapper.UserMapperForChat;
import com.sales.chats.repositories.ChatHbRepository;
import com.sales.chats.repositories.ChatRepository;
import com.sales.chats.repositories.ContactRepository;
import com.sales.claims.AuthUser;
import com.sales.entities.Contact;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ContactsService {

    private static final Logger logger = LoggerFactory.getLogger(ContactsService.class);
    private final ContactRepository contactRepository;
    private final ChatRepository chatRepository;
    private final WholesaleUserRepository wholesaleUserRepository;
    private final ChatHbRepository chatHbRepository;
    private final ContactMapper contactMapper;
    private final UserMapperForChat userMapper;


    @Transactional
    public List<UserDto> getAllContactsByUserId(AuthUser loggedUser, HttpServletRequest request) {
        logger.debug("Starting getAllContactsByUserId method");
        List<User> userList = contactRepository.getContactByUserId(loggedUser.getId()).stream().filter(Objects::nonNull).toList();
        for (User user : userList) {
            Integer unSeenChatsCount = chatRepository.getUnSeenChatsCount(user.getSlug(), loggedUser.getSlug());
            String hostUrl = Utils.getHostUrl(request);
            user.setAvatarUrl(hostUrl + GlobalConstant.WHOLESALER_IMAGE_PATH + user.getSlug() + GlobalConstant.PATH_SEPARATOR + user.getAvatar());
            user.setChatNotification(unSeenChatsCount);
            //user.setBlocked(blockListService.isReceiverBlockedBySender(loggedUser,user));
            // Verifying the contact user existing in chats and sender chat request accepted or not.
            //user.setAccepted(chatUserRepository.getSenderAcceptStatus(loggedUser.getId(),user));
        }
        logger.debug("Completed getAllContactsByUserId method");
        return userList.stream().map(userMapper::toDto).toList();
    }


    @Transactional
    public ContactDto addNewContact(AuthUser loggedUser, String contactSlug) {
        logger.debug("Starting addNewContact method loggedUser slug : {} and contactSlug : {}", loggedUser.getSlug(), contactSlug);
        // TODO : check if user already in contact list not chat list
//        Integer userFound = chatRepository.isUserExistsInChatList(loggedUser.getSlug(), contactSlug);
//        if (userFound > 0) {
//            logger.debug("User already exists in chat list, returning null");
//            return null;
//        }
        User contactUser = wholesaleUserRepository.findUserBySlug(contactSlug);
        if (contactUser == null) {
            logger.error("Not a valid contact");
            throw new MyException(ResponseMessages.NOT_A_VALID_CONTACT);
        }
        Contact contacts = Contact.builder()
                .userId(loggedUser.getId())
                .contactUser(contactUser)
                .build();
        Contact savedContact = contactRepository.save(contacts); // Create operation
        logger.debug("Completed addNewContact method");
        return contactMapper.toDto(savedContact);
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public int removeContact(AuthUser loggedUser, String contactUserSlug, Boolean deleteChats) {
        logger.debug("Going to remove contact from contact list with loggedUser  {} : and contactUserSlug {} ", loggedUser, contactUserSlug);
        User contactUser = wholesaleUserRepository.findUserBySlug(contactUserSlug);
        if (contactUser == null) throw new NotFoundException(ResponseMessages.NO_CONTACT_USER_FOUND_TO_DELETE);
        Integer deleted = contactRepository.deleteContactUserFromContact(loggedUser.getId(), contactUser);
        if (deleted > 0 && deleteChats) {
            chatHbRepository.deleteChats(loggedUser.getSlug(), contactUserSlug);
        }
        return deleted;
    }

}
