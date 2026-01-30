package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.GenderEnum;
import com.caochung.recruitment.util.annotation.Gender;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class GenderValidator implements ConstraintValidator<Gender, String> {

    private List<String> genders;

    @Override
    public void initialize(Gender constraintAnnotation) {
        this.genders = Arrays.stream(constraintAnnotation.anyOf())
                .map(Enum::name)
                .toList();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || genders.contains(value);
    }
}
