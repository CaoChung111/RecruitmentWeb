package com.caochung.recruitment.ai.dto;

import java.util.Map;

public record SemanticSearchResultDTO(
        String id,
        String title,
        String content,
        double similarityScore,
        Map<String, Object> metadata
) {
}
