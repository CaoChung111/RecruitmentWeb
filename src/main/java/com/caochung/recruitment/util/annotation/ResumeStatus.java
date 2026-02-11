package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.util.ResumeStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = ResumeStatusValidator.class)
public @interface ResumeStatus {
    ResumeStatusEnum[] anyOf() default {};
    String message() default "Invalid Resume Status, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
