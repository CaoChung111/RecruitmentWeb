package com.caochung.recruitment.service;

import com.caochung.recruitment.ai.dto.ParsedCvDTO;

import com.caochung.recruitment.dto.response.ResumeDetailResponseDTO;

public interface ResumeDetailService {
    void saveParsedResult(Long resumeId, ParsedCvDTO parsedCv);
    void markAsFailed(Long resumeId);
    void markAsProcessing(Long resumeId);
    ResumeDetailResponseDTO getParsedResumeDetail(Long resumeId);
}
