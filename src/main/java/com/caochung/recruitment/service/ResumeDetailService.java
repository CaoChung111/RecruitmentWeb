package com.caochung.recruitment.service;

import com.caochung.recruitment.ai.dto.ParsedCvDTO;

public interface ResumeDetailService {
    void saveParsedResult(Long resumeId, ParsedCvDTO parsedCv);
    void markAsFailed(Long resumeId);
    void markAsProcessing(Long resumeId);
}
