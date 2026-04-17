package com.sales.chats.mapper;


import com.sales.chats.dto.ContactDto;
import com.sales.entities.Contact;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContactMapper {
    ContactDto toDto(Contact contact);
}
