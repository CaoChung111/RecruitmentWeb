package com.caochung.recruitment.ai.dto.tool;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record JobDetailToolResponse(
        Long id,
        String name,
        String companyName,
        String location,
        double salary,
        String level,
        int quantity,
        String description,
        String status
) implements Serializable {
}
