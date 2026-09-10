package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.ai.dto.ParsedCvDTO;
import com.caochung.recruitment.constant.AnalysisStatusEnum;
import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.domain.ResumeDetail;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.ResumeDetailRepository;
import com.caochung.recruitment.repository.ResumeRepository;
import com.caochung.recruitment.service.ResumeDetailService;
import com.caochung.recruitment.service.mapper.ResumeDetailMapper;
import com.caochung.recruitment.dto.response.ResumeDetailResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j(topic = "RESUME-DETAIL-SERVICE")
@RequiredArgsConstructor
public class ResumeDetailServiceImpl implements ResumeDetailService {

    private final ResumeDetailRepository resumeDetailRepository;
    private final ResumeDetailMapper resumeDetailMapper;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ResumeDetailResponseDTO getParsedResumeDetail(Long resumeId) {
        ResumeDetail detail = resumeDetailRepository.findByResume_Id(resumeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_DETAIL_NOT_FOUND));

        List<String> skillsList = Collections.emptyList();
        if (detail.getSkills() != null && !detail.getSkills().isBlank()) {
            try {
                skillsList = objectMapper.readValue(detail.getSkills(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse skills JSON for resumeId={}", resumeId);
            }
        }

        List<ParsedCvDTO.ExperienceDTO> experiencesList = Collections.emptyList();
        if (detail.getExperiences() != null && !detail.getExperiences().isBlank()) {
            try {
                experiencesList = objectMapper.readValue(detail.getExperiences(), new TypeReference<List<ParsedCvDTO.ExperienceDTO>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse experiences JSON for resumeId={}", resumeId);
            }
        }

        return ResumeDetailResponseDTO.builder()
                .id(detail.getId())
                .resumeId(resumeId)
                .fullName(detail.getFullName())
                .email(detail.getEmail())
                .phone(detail.getPhone())
                .skills(skillsList)
                .yearsOfExperience(detail.getYearsOfExperience())
                .currentPosition(detail.getCurrentPosition())
                .summary(detail.getSummary())
                .education(detail.getEducation())
                .experiences(experiencesList)
                .analysisStatus(detail.getAnalysisStatus())
                .build();
    }

    @Override
    @Transactional
    public void saveParsedResult(Long resumeId, ParsedCvDTO parsedCv) {
        if(parsedCv == null){
            log.warn("ParsedCvDTO is null for resumeId={}, marking as FAILED", resumeId);
            markAsFailed(resumeId);
            return;
        }
        ResumeDetail resumeDetail = getOrCreateResumeDetail(resumeId);
        resumeDetailMapper.updateResumeDetail(parsedCv,  resumeDetail);
        resumeDetail.setAnalysisStatus(AnalysisStatusEnum.COMPLETED);
        resumeDetailRepository.save(resumeDetail);
        log.info("Saved resume details with status COMPLETED for resume id {}", resumeId);
    }

    @Override
    @Transactional
    public void markAsFailed(Long resumeId) {
        ResumeDetail detail = getOrCreateResumeDetail(resumeId);
        detail.setAnalysisStatus(AnalysisStatusEnum.FAILED);
        resumeDetailRepository.save(detail);
        log.warn("Resume id={} analysis status updated to FAILED", resumeId);
    }

    @Override
    @Transactional
    public void markAsProcessing(Long resumeId) {
        ResumeDetail detail = getOrCreateResumeDetail(resumeId);
        detail.setAnalysisStatus(AnalysisStatusEnum.PROCESSING);
        resumeDetailRepository.save(detail);
        log.info("Resume id={} analysis status updated to PROCESSING", resumeId);
    }

    private ResumeDetail getOrCreateResumeDetail(Long resumeId) {
        return resumeDetailRepository.findByResume_Id(resumeId)
                .orElseGet(() -> {
                    Resume resume = resumeRepository.findById(resumeId)
                            .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
                    return ResumeDetail.builder()
                            .resume(resume)
                            .analysisStatus(AnalysisStatusEnum.PENDING)
                            .build();
                });
    }
}
