package com.sales.wholesaler.mapper;


import com.sales.entities.StoreNotifications;
import com.sales.wholesaler.dto.WholesaleStoreNotificationDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesaleStoreNotificationMapper {
    WholesaleStoreNotificationDto toDto(StoreNotifications storeNotifications);
}
