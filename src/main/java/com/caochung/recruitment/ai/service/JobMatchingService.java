package com.caochung.recruitment.ai.service;

import com.caochung.recruitment.ai.dto.JobMatchResultDTO;

public interface JobMatchingService {
    JobMatchResultDTO calculateJobScore(Long resumeId, Long jobId);
}
