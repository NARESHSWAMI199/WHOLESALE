package com.sales.admin.mapper;


import com.sales.admin.dto.StoreNotificationDto;
import com.sales.entities.StoreNotifications;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StoreNotificationMapper {
    StoreNotificationDto toDto(StoreNotifications storeNotifications);
}
