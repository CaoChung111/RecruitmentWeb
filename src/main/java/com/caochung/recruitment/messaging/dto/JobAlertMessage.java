package com.caochung.recruitment.messaging.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Jacksonized
public class JobAlertMessage implements Serializable {
    @Builder.Default
    private String messageId = UUID.randomUUID().toString();

    @Builder.Default
    private Instant createdAt = Instant.now();

    private String subscriberEmail;
    private String subscriberName;
    private List<JobSummaryMessage> matchedJobs;

    @Getter
    @Builder
    @Jacksonized
    public static class JobSummaryMessage implements Serializable {
         private String name;
         private String companyName;
         private String location;
         private Double salary;
         private String level;
    }
}
