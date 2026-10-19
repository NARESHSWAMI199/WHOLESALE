package com.sales.request;

import com.sales.events.event.EventType;
import com.sales.request.enums.NOTIFICATION_EVENT_FOR;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record NotificationRequest(
        String fcmToken,
        String email,
        String mobileNo,
        @NotNull
        List<EventType> eventTypeList,
        Map<String, String> placeHolders,
        @NotNull
        NOTIFICATION_EVENT_FOR notificationEventFor,
        String userSlug) {
}
