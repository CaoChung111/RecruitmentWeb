package com.caochung.recruitment.controller;

import com.caochung.recruitment.ai.dto.ChatRequestDTO;
import com.caochung.recruitment.ai.dto.JobMatchResultDTO;
import com.caochung.recruitment.ai.dto.RecommendedJobDTO;
import com.caochung.recruitment.ai.dto.SemanticSearchResultDTO;
import com.caochung.recruitment.ai.service.AiChatbotService;
import com.caochung.recruitment.ai.service.JobMatchingService;
import com.caochung.recruitment.ai.service.RecommendationService;
import com.caochung.recruitment.ai.service.SemanticSearchService;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.dto.response.ResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI & Smart Recruitment", description = "Tập hợp các API thông minh sử dụng Spring AI, Gemini & Vector Search.")
public class AiController {

    private final JobMatchingService jobMatchingService;
    private final AiChatbotService aiChatbotService;
    private final SemanticSearchService semanticSearchService;
    private final RecommendationService recommendationService;

    @Operation(summary = "Chấm điểm so khớp CV và JD", description = "Tính điểm phù hợp (0-100), phân tích điểm mạnh, điểm yếu và kỹ năng còn thiếu giữa CV và JD bằng Gemini AI (Có Redis Caching).")
    @GetMapping("/match-score")
    public ResponseData<JobMatchResultDTO> getMatchScore(
            @RequestParam Long resumeId, @RequestParam Long jobId) {
        JobMatchResultDTO matchResultDTO = this.jobMatchingService.calculateJobScore(resumeId, jobId);
        return ResponseData.success(matchResultDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Hội thoại với Trợ lý AI", description = "Chat với trợ lý tuyển dụng RecruitAI, tự động kích hoạt Function Calling để tra cứu công việc thực tế trong Database.")
    @PostMapping("/chat")
    public ResponseData<String> chat(@Valid @RequestBody ChatRequestDTO request) {
        String answer = this.aiChatbotService.chatWithAssistant(request);
        return ResponseData.success(answer, SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Đồng bộ Vector Store cho Job", description = "Tạo Embeddings và nạp toàn bộ Job đang hoạt động vào Vector Store để phục vụ tìm kiếm ngữ nghĩa.")
    @PostMapping("/index-jobs")
    public ResponseData<String> indexJobs() {
        this.semanticSearchService.indexAllJobs();
        return ResponseData.success("Đồng bộ dữ liệu Vector Store thành công", SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Tìm kiếm việc làm theo ngữ nghĩa", description = "Tìm kiếm công việc dựa trên độ tương đồng Cosine (Semantic Search) thay vì từ khóa SQL LIKE truyền thống.")
    @GetMapping("/semantic-search")
    public ResponseData<List<SemanticSearchResultDTO>> semanticSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(defaultValue = "0.6") double minSimilarity) {
        List<SemanticSearchResultDTO> results = this.semanticSearchService.searchJobsSemantically(query, topK, minSimilarity);
        return ResponseData.success(results, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Gợi ý việc làm thông minh cho ứng viên", description = "Cơ chế Two-Stage Recommendation: Lọc nhanh bằng Vector Search kết hợp chấm điểm sâu bằng Gemini AI.")
    @GetMapping("/recommendations")
    public ResponseData<List<RecommendedJobDTO>> getRecommendations(
            @RequestParam Long resumeId,
            @RequestParam(defaultValue = "5") int limit) {
        List<RecommendedJobDTO> recommendations = this.recommendationService.recommendJobForResume(resumeId, limit);
        return ResponseData.success(recommendations, SuccessCode.GET_SUCCESS);
    }

}
