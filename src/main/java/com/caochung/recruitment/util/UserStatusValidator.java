package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.util.annotation.CompanyStatus;
import com.caochung.recruitment.util.annotation.UserStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class UserStatusValidator implements ConstraintValidator<UserStatus, String> {
    private List<String> userStatus;

    @Override
    public void initialize(UserStatus constraintAnnotation) {
        var subset = constraintAnnotation.anyOf();

        if(subset.length == 0){
            userStatus = Arrays.stream(UserStatusEnum.values())
                    .map(Enum::name).toList();
        }
        else{
            userStatus = Arrays.stream(subset)
                    .map(Enum::name).toList();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null || userStatus.contains(value);
    }
}
