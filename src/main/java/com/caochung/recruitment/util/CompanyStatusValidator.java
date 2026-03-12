package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.util.annotation.CompanyStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompanyStatusValidator implements ConstraintValidator<CompanyStatus, String> {
    private List<String> companyStatus;

    @Override
    public void initialize(CompanyStatus constraintAnnotation) {
        var subset = constraintAnnotation.anyOf();

        if(subset.length == 0){
            companyStatus = Arrays.stream(CompanyStatusEnum.values())
                    .map(Enum::name).toList();
        }
        else{
            companyStatus = Arrays.stream(subset)
                    .map(Enum::name).toList();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null || companyStatus.contains(value);
    }
}
