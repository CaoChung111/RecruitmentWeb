package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.util.JobStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = JobStatusValidator.class)
public @interface JobStatus {
    JobStatusEnum[] anyOf() default {};
    String message() default "Invalid Job Status, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
