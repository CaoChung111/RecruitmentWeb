package com.caochung.recruitment.messaging.publisher;

import com.caochung.recruitment.messaging.dto.EmailNotificationMessage;

public interface NotificationPublisher {

    void publish(EmailNotificationMessage message);
}
