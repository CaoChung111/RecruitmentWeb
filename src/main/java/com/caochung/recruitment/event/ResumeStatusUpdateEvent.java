package com.caochung.recruitment.event;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Builder
public class ResumeStatusUpdateEvent {
    private String emailTo;
    private String username;
    private String jobName;
    private String companyName;
    private ResumeStatusEnum status;
}
