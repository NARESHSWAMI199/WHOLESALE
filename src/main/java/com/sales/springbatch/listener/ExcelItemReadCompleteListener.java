package com.sales.springbatch.listener;

import com.sales.claims.AuthUser;
import com.sales.events.event.EventType;
import com.sales.events.publisher.NotificationPublisher;
import com.sales.request.NotificationRequest;
import com.sales.request.enums.NOTIFICATION_EVENT_FOR;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.util.List;

@RequiredArgsConstructor
public class ExcelItemReadCompleteListener implements JobExecutionListener {

    private final NotificationPublisher notificationPublisher;
    private final AuthUser loggedUser;

    @Override
    public void afterJob(@NonNull JobExecution jobExecution) {
        NotificationRequest notificationEvent = NotificationRequest.builder()
                .eventTypeList(List.of(EventType.IN_APP))
                .fcmToken(loggedUser.getFcmToken())
                .notificationEventFor(NOTIFICATION_EVENT_FOR.LOGIN_OTP)
                .userSlug(loggedUser.getSlug())
                .build();
        notificationPublisher.sendNotification(notificationEvent); // sending in app notification excel writing
    }
}
