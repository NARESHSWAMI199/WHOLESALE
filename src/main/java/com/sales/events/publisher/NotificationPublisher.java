package com.sales.events.publisher;


import com.sales.admin.repositories.SupportEmailsRepository;
import com.sales.events.event.EventType;
import com.sales.events.event.NotificationEvent;
import com.sales.request.NotificationRequest;
import com.sales.request.enums.NOTIFICATION_EVENT_FOR;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final KafkaTemplate<String, List<NotificationEvent>> kafkaTemplate;

    public void sendNotification(NotificationRequest notificationRequest) {
        List<NotificationEvent> notificationEvents = notificationRequest.eventTypeList().stream().map(eventType -> {
            NotificationEvent.NotificationEventBuilder notificationEventBuilder = switch (eventType) {
                case EMAIL -> NotificationEvent.builder()
                        .templateKey(notificationRequest.notificationEventFor().getEmail())
                        .recipient(notificationRequest.email());
                case SMS -> NotificationEvent.builder().templateKey(notificationRequest.notificationEventFor().getSms())
                        .recipient(notificationRequest.mobileNo());
                case IN_APP ->
                        NotificationEvent.builder().templateKey(notificationRequest.notificationEventFor().getPush())
                                .recipient(notificationRequest.fcmToken());
                default -> throw new IllegalStateException("Unexpected value: " + eventType);
            };
            return notificationEventBuilder
                    .channel(eventType)
                    .idempotencyKey(UUID.randomUUID().toString())
                    .userSlug(notificationRequest.userSlug())
                    .build();
        }).toList();
        kafkaTemplate.send("notification-topic", notificationEvents);
    }

}
