package com.caochung.recruitment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JobAlertEmailDTO {
    private String userName;
    private String jobName;
    private String companyName;
    private Double salary;
    private String location;
}
