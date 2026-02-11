package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.LevelEnum;
import com.caochung.recruitment.util.LevelValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = LevelValidator.class)
public @interface Level {
    LevelEnum[] anyOf() default {};
    String message() default "Invalid Level, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
