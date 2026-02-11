package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.util.annotation.ResumeStatus;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.ResumeStatusEnum.*;
import static com.caochung.recruitment.constant.ResumeStatusEnum.REJECTED;

@Getter @Setter
public class ResumeUpdateDTO {
    @ResumeStatus(anyOf = {PENDING, REVIEWING, APPROVED, REJECTED})
    private String status;
}
