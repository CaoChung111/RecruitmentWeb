package com.caochung.recruitment.ai.service.impl;

import com.caochung.recruitment.ai.dto.JobMatchResultDTO;
import com.caochung.recruitment.ai.dto.RecommendedJobDTO;
import com.caochung.recruitment.ai.dto.SemanticSearchResultDTO;
import com.caochung.recruitment.ai.service.JobMatchingService;
import com.caochung.recruitment.ai.service.RecommendationService;
import com.caochung.recruitment.ai.service.SemanticSearchService;
import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.ResumeDetail;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.ResumeDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "RECOMMENDATION-SERVICE")
public class RecommendationServiceImpl implements RecommendationService {

    private final ResumeDetailRepository resumeDetailRepository;
    private final SemanticSearchService semanticSearchService;
    private final JobMatchingService jobMatchingService;

    @Override
    public List<RecommendedJobDTO> recommendJobForResume(Long resumeId, int limit) {
        log.info("Generating AI recommendations for resumeId={}", resumeId);
        ResumeDetail resumeDetail = resumeDetailRepository.findByResume_Id(resumeId)
                .orElseThrow(()-> new AppException(ErrorCode.RESUME_DETAIL_NOT_FOUND));

        // Tạo truy vấn từ kỹ năng và kinh nghiệm của candidate
        String candidateProfileQuery = String.format("Kỹ năng: %s. Kinh nghiệm: %s", resumeDetail.getSkills(),resumeDetail.getExperiences());

        // Lấy 10 job tiềm năng
        List<SemanticSearchResultDTO> potentialJobs = semanticSearchService.searchJobsSemantically(candidateProfileQuery, 10, 0.5);

        // Xếp hạng bằng JobMatchingService
        List<RecommendedJobDTO> recommendations = new ArrayList<>();

        for(SemanticSearchResultDTO job: potentialJobs){
            Long jobId = Long.valueOf(job.id());
            try {
                JobMatchResultDTO matchResult = jobMatchingService.calculateJobScore(resumeId, jobId);
                if(matchResult.overallScore()>=60){
                    recommendations.add(new RecommendedJobDTO(
                            jobId, job.title(),
                            (String) job.metadata().getOrDefault("companyName", "N/A"),
                            matchResult.overallScore(),
                            matchResult.summary(),
                            matchResult.strengths()
                    ));
                }
            } catch (Exception e) {
                log.error("Error calculating deep match score for jobId={}", jobId, e);
            }
        }

        return recommendations.stream()
                .sorted(Comparator.comparing(RecommendedJobDTO::matchScore).reversed())
                .limit(limit)
                .toList();
    }
}
