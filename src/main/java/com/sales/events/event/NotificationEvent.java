package com.sales.events.event;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record NotificationEvent (
        @NotNull
        String recipient,
        String idempotencyKey,
        String userSlug,
        EventType channel,
        @NotNull
        String templateKey,
        Map<String, String> placeholders,

        String emailOverride,
        String smsMobileOverride,
        String firebaseTokenOverride
    ) {}

