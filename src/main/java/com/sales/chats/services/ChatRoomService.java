package com.sales.chats.services;


import com.sales.chats.repositories.ChatRoomHbRepository;
import com.sales.chats.repositories.ChatRoomRepository;
import com.sales.claims.AuthUser;
import com.sales.global.ResponseMessages;
import com.sales.request.ChatRoomDto;
import com.sales.entities.ChatRoom;
import com.sales.entities.ChatRoomUser;
import com.sales.entities.User;
import com.sales.exceptions.NotFoundException;
import com.sales.utils.Utils;
import com.sales.wholesaler.repository.WholesaleUserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService  {


    private final ChatRoomRepository chatRoomRepository;
    private final WholesaleUserRepository wholesaleUserRepository;
    private final ChatRoomHbRepository chatRoomHbRepository;
    public List<ChatRoom> getAllChatRoom(){
        return chatRoomRepository.findAll();
    }


    @Transactional(rollbackFor = {NotFoundException.class, RuntimeException.class,Exception.class})
    public ChatRoom createRoom(ChatRoomDto chatRoomDto) {
        // saving room
        ChatRoom chatRoom = ChatRoom.builder()
                .slug(UUID.randomUUID().toString())
                .name(chatRoomDto.getName())
                .description(chatRoomDto.getDescription())
                .createdAt(Utils.getCurrentMillis())
                .updatedAt(Utils.getCurrentMillis())
                .build();
        ChatRoom inserted =  chatRoomRepository.save(chatRoom);


        List<ChatRoomUser> chatRoomUsers = new ArrayList<>();
        for(String slug :chatRoomDto.getUsers()) {
            User user = wholesaleUserRepository.findUserBySlug(slug);
            if(user == null) throw new NotFoundException(ResponseMessages.CHAT_USERS_ARE_NOT_VALID);
            ChatRoomUser chatRoomUser = ChatRoomUser.builder()
                    .user(user)
                    .roomId(inserted.getId())
                    .createdAt(Utils.getCurrentMillis())
                    .build();
            chatRoomUsers.add(chatRoomUser);
        }
        // this will auto save
        inserted.setChatRoomUsers(chatRoomUsers);

        return  inserted;
    }



    @Transactional
    public int updateRoom(ChatRoomDto chatRoomDto, AuthUser loggedUser) {
        return chatRoomHbRepository.updateChatRoom(chatRoomDto);
    }




}
