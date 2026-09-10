package com.caochung.recruitment.ai.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.io.Serializable;
import java.util.List;

public record JobMatchResultDTO (
        @JsonPropertyDescription("Điểm số phù hợp tổng thể trên thang điểm 100 (từ 0 đến 100)")
        int overallScore,
        @JsonPropertyDescription("Mức độ phù hợp tổng quan: HIGH (>=75), MEDIUM (50-74), LOW (<50)")
        MatchLevel matchLevel,
        @JsonPropertyDescription("Tóm tắt đánh giá ngắn gọn về độ phù hợp trong 2-3 câu")
        String summary,
        @JsonPropertyDescription("Danh sách các điểm mạnh vượt trội của ứng viên so với yêu cầu của JD")
        List<String> strengths,
        @JsonPropertyDescription("Danh sách các điểm yếu hoặc điểm còn hạn chế của ứng viên")
        List<String> weaknesses,
        @JsonPropertyDescription("Danh sách các kỹ năng bắt buộc trong JD mà ứng viên chưa có hoặc chưa thể hiện rõ")
        List<String> missingSkills,
        @JsonPropertyDescription("Điểm số chi tiết từng phần theo thang điểm 100")
        DetailedScores detailedScores,
        @JsonPropertyDescription("Đề xuất câu hỏi phỏng vấn đào sâu cho HR")
        List<String> suggestedInterviewQuestions
) implements Serializable {

    public enum MatchLevel {
        HIGH, MEDIUM, LOW
    }

    public record DetailedScores(
            @JsonPropertyDescription("Điểm phù hợp về Kỹ năng chuyên môn (0-100)")
            int technicalSkillScore,
            @JsonPropertyDescription("Điểm phù hợp về Số năm và Cấp độ kinh nghiệm (0-100)")
            int experienceScore,
            @JsonPropertyDescription("Điểm phù hợp về Trình độ học vấn và Chứng chỉ (0-100)")
            int educationScore,
            @JsonPropertyDescription("Điểm phù hợp về Kỹ năng mềm và Ngoại ngữ (0-100)")
            int softSkillScore
    ) implements Serializable {}
}
