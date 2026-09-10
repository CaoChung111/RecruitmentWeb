package com.caochung.recruitment.ai.service.impl;

import com.caochung.recruitment.ai.dto.JobMatchResultDTO;
import com.caochung.recruitment.ai.service.JobMatchingService;
import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.ResumeDetail;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.ResumeDetailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "JOB-MATCHING-SERVICE")
public class JobMatchingServiceImpl implements JobMatchingService {

    private final ChatClient chatClient;
    private final ResumeDetailRepository resumeDetailRepository;
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;

    private static final String CACHE_KEY_PREFIX = "job_match:resume_%d:job_%d";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private static final String SYSTEM_PROMPT = """
            Bạn là Giám đốc Kỹ thuật (CTO) kiêm Chuyên gia Tuyển dụng Cao cấp.
            Nhiệm vụ của bạn là so sánh chi tiết giữa Hồ sơ Ứng viên (CV) và Bản mô tả Công việc (JD) để đưa ra đánh giá khách quan, chính xác nhất.
            
            NGUYÊN TẮC ĐÁNH GIÁ:
            1. Không tự suy đoán hoặc phóng đại kỹ năng của ứng viên.
            2. Trọng số: Kỹ năng chuyên môn (40%), Kinh nghiệm thực tế (35%), Học vấn/Chứng chỉ (15%), Kỹ năng mềm (10%).
            3. Trả về kết quả TUÂN THỦ HOÀN TOÀN cấu trúc JSON sau:
            {format}
            """;


    @Override
    public JobMatchResultDTO calculateJobScore(Long resumeId, Long jobId) {
        String cacheKey = String.format(CACHE_KEY_PREFIX, resumeId, jobId);

        // Kiểm tra Cache
        String cacheValue = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null && !cacheValue.isBlank()) {
            try {
                log.info("CACHE HIT: Match score for resumeId={}, jobId={}", resumeId, jobId);
                return objectMapper.readValue(cacheValue, JobMatchResultDTO.class);
            } catch ( JsonProcessingException e) {
                log.warn("Failed to deserialize cached match result, fallback to AI calculation", e);
            }
        }

        // Không có trong cache thì lấy từ db
        log.info("CACHE MISS: Calculating match score via Gemini AI for resumeId={}, jobId={}", resumeId, jobId);
        ResumeDetail resumeDetail = resumeDetailRepository.findByResume_Id(resumeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_DETAIL_NOT_FOUND));

        Job job = jobRepository.findById(jobId).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        // Tạo structure output converter
        BeanOutputConverter<JobMatchResultDTO>  converter = new BeanOutputConverter<>(JobMatchResultDTO.class);

        String userPrompt = String.format("""
                DỮ LIỆU HỒ SƠ ỨNG VIÊN (CV):
                - Họ tên: %s
                - Tóm tắt: %s
                - Kỹ năng (JSON): %s
                - Năm kinh nghiệm: %s
                - Kinh nghiệm làm việc (JSON): %s
                - Học vấn (JSON): %s
                
                ----------------------------------------
                DỮ LIỆU BẢN MÔ TẢ CÔNG VIỆC (JD):
                - Tiêu đề: %s
                - Cấp bậc (Level): %s
                - Địa điểm: %s
                - Mức lương: %s
                - Kỹ năng: %s
                - Mô tả chi tiết: %s
                """,
                resumeDetail.getFullName(),
                resumeDetail.getSummary(),
                resumeDetail.getSkills(),
                resumeDetail.getYearsOfExperience(),
                resumeDetail.getExperiences(),
                resumeDetail.getEducation(),
                job.getName(),
                job.getLevel(),
                job.getLocation(),
                job.getSalary(),
                job.getSkills().stream().map(Object::toString).collect(Collectors.joining(", ")),
                job.getDescription()
                );

        // Gọi LLM
        String response = chatClient.prompt()
                .system(sp-> sp.text(SYSTEM_PROMPT)
                        .param("format", converter.getFormat()))
                .user(userPrompt)
                .call()
                .content();

        log.info("Gemini caculated match score successfully. Converting response to ParsedCvDTO");
        // Chuyển JSON -> jobMatchResultDTO
        JobMatchResultDTO jobMatchResultDTO = converter.convert(response);

        // Lưu vào cache
        try{
            String jsonToCache = objectMapper.writeValueAsString(jobMatchResultDTO);
            redisTemplate.opsForValue().set(cacheKey, jsonToCache, CACHE_TTL);
            log.info("Successfully cached match result for key: {}", cacheKey);
        }catch (JsonProcessingException e){
            log.warn("Failed to deserialize cached match result, fallback to AI calculation", e);
        }
        return jobMatchResultDTO;
    }
}
