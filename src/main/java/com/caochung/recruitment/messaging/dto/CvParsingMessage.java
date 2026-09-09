package com.caochung.recruitment.messaging.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Jacksonized
public class CvParsingMessage implements Serializable {
    @Builder.Default
    private String messageId = UUID.randomUUID().toString();

    @Builder.Default
    private Instant createdAt = Instant.now();

    private long resumeId;
    private String cloudinaryUrl;
    private String candidateEmail;
    private String candidateName;
}
