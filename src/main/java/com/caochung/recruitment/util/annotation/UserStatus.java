package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.util.CompanyStatusValidator;
import com.caochung.recruitment.util.UserStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = UserStatusValidator.class)
public @interface UserStatus {
    UserStatusEnum[] anyOf() default {};
    String message() default "Invalid User Status, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
