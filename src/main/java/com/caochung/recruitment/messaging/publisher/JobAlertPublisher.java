package com.caochung.recruitment.messaging.publisher;

import com.caochung.recruitment.messaging.dto.JobAlertMessage;

public interface JobAlertPublisher {
    void publish(JobAlertMessage message);
}
