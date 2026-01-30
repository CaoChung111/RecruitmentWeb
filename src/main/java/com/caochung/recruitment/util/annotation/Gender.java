package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.GenderEnum;
import com.caochung.recruitment.util.GenderValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenderValidator.class)
public @interface Gender {
    GenderEnum[] anyOf();
    String message() default "Invalid Gender, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
