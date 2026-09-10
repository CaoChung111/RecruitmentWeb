package com.caochung.recruitment.ai.dto.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record JobDetailToolRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("ID định danh của công việc cần tra cứu chi tiết")
        Long jobId
) {
}
