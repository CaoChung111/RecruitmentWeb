package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.util.annotation.ResumeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.ResumeStatusEnum.*;

@Getter @Setter
public class ResumeUpdateDTO {
    @NotNull(message = "Status cannot be null")
    @ResumeStatus(anyOf = {PENDING, REVIEWING, APPROVED, REJECTED})
    private String status;
}
