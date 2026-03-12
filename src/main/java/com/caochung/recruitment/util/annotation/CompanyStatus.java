package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.util.CompanyStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = CompanyStatusValidator.class)
public @interface CompanyStatus {
    CompanyStatusEnum[] anyOf() default {};
    String message() default "Invalid Company Status, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
