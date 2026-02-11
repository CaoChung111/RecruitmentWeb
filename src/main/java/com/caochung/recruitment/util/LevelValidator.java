package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.LevelEnum;
import com.caochung.recruitment.util.annotation.Level;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class LevelValidator implements ConstraintValidator<Level,String> {
    private List<String> levels;

    @Override
    public void initialize(Level constraintAnnotation) {
        var subset = constraintAnnotation.anyOf();

        if (subset.length == 0) {
            this.levels = Arrays.stream(LevelEnum.values())
                    .map(Enum::name)
                    .toList();
        } else {
            this.levels = Arrays.stream(subset)
                    .map(Enum::name)
                    .toList();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && levels.contains(value);
    }
}
