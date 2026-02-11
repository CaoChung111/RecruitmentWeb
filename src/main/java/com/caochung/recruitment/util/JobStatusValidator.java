package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.constant.LevelEnum;
import com.caochung.recruitment.util.annotation.JobStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JobStatusValidator implements ConstraintValidator<JobStatus, String> {
    private List<String> jobStatus;

    @Override
    public void initialize(JobStatus constraintAnnotation) {
        var subset = constraintAnnotation.anyOf();

        if (subset.length == 0) {
            this.jobStatus = Arrays.stream(JobStatusEnum.values())
                    .map(Enum::name)
                    .toList();
        } else {
            this.jobStatus = Arrays.stream(subset)
                    .map(Enum::name)
                    .toList();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && jobStatus.contains(value);
    }
}
