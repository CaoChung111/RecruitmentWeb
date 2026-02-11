package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.util.annotation.ResumeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.ResumeStatusEnum.*;

@Getter @Setter
@Builder
public class ResumeRequestDTO {
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "CV cannot be empty")
    private String url;

    @ResumeStatus(anyOf = {PENDING, REVIEWING, APPROVED, REJECTED})
    private String status;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Job ID cannot be null")
    private Long jobId;
}
