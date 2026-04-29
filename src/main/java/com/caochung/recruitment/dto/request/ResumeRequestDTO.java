package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.util.annotation.ResumeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.ResumeStatusEnum.*;

@Getter @Setter
@Builder
public class ResumeRequestDTO {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email is not in the correct format")
    private String email;

    @NotBlank(message = "CV URL cannot be empty")
    @Size(max = 500, message = "CV URL must not exceed 500 characters")
    private String url;

    @NotNull(message = "Status cannot be null")
    @ResumeStatus(anyOf = {PENDING, REVIEWING, APPROVED, REJECTED})
    private String status;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Job ID cannot be null")
    private Long jobId;
}
