package com.caochung.recruitment.messaging.publisher;

import com.caochung.recruitment.messaging.dto.CvParsingMessage;

public interface CvParsingPublisher {
    void publish(CvParsingMessage message);
}
