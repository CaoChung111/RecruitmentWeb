package com.caochung.recruitment.ai.dto.tool;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

public record JobSummaryToolResponse(
        int totalFound,
        List<JobItem> jobs
) implements Serializable {
    @Builder
    public record JobItem(
       Long id,
       String title,
       String companyName,
       String location,
       double salary,
       String level
    ) implements Serializable {}
}
