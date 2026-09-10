package com.caochung.recruitment.ai.service;

import com.caochung.recruitment.ai.dto.RecommendedJobDTO;

import java.util.List;

public interface RecommendationService {
    List<RecommendedJobDTO> recommendJobForResume(Long resumeId, int limit);
}
