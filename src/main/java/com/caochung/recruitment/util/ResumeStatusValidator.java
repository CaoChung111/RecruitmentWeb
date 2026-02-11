package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.util.annotation.ResumeStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResumeStatusValidator implements ConstraintValidator<ResumeStatus, String> {
    private List<String> resumeStatus;

    @Override
    public void initialize(ResumeStatus constraintAnnotation) {
        var subset = constraintAnnotation.anyOf();
        if(subset.length == 0){
            this.resumeStatus = Arrays.stream(ResumeStatusEnum.values())
                    .map(Enum::name).toList();
        }
        else{
            this.resumeStatus = Arrays.stream(subset)
                    .map(Enum::name).toList();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && resumeStatus.contains(value);
    }
}
