package com.sales.events.event;


import com.sales.admin.dto.StoreNotificationDto;
import com.sales.entities.StoreNotifications;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NotificationEvent implements Serializable {
    private List<EventType> eventType;
    private String userSlug;
    private StoreNotificationDto notification;
}


