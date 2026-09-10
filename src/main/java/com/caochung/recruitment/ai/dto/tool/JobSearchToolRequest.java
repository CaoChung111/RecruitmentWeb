package com.caochung.recruitment.ai.dto.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record JobSearchToolRequest(
        @JsonProperty(required = false)
        @JsonPropertyDescription("Từ khóa tìm kiếm công việc (ví dụ: Java, ReactJS, Node.js, Tester)")
        String keyword,

        @JsonProperty(required = false)
        @JsonPropertyDescription("Địa điểm làm việc (ví dụ: Hà Nội, TP.HCM, Đà Nẵng, Remote)")
        String location,
        @JsonProperty(required = false)
        @JsonPropertyDescription("Mức lương tối thiểu mong muốn theo tháng (đơn vị: USD hoặc VND)")
        Double minSalary,
        @JsonProperty(required = false)
        @JsonPropertyDescription("Cấp bậc công việc: INTERN, FRESHER, JUNIOR, MIDDLE, SENIOR")
        String level
) { }
