package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.constant.LevelEnum;
import com.caochung.recruitment.util.annotation.JobStatus;
import com.caochung.recruitment.util.annotation.Level;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

import static com.caochung.recruitment.constant.JobStatusEnum.*;
import static com.caochung.recruitment.constant.LevelEnum.*;

@Getter
@Setter
public class JobRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 200, message = "Job name must be between 2 and 200 characters")
    private String name;

    @NotBlank(message = "Location cannot be empty")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @NotNull(message = "Salary cannot be null")
    @Positive(message = "Salary must be a positive number")
    private Double salary;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be a positive number")
    private Integer quantity;

    @NotNull(message = "Level cannot be null")
    @Level(anyOf = {FRESHER, INTERN, JUNIOR, MIDDLE, SENIOR, LEAD})
    private String level;

    @Size(max = 10000, message = "Description must not exceed 10000 characters")
    private String description;

    @NotNull(message = "Start date cannot be null")
    private Instant startDate;

    @NotNull(message = "End date cannot be null")
    @Future(message = "End date must be in the future")
    private Instant endDate;

    @NotNull(message = "Job status cannot be null")
    @JobStatus(anyOf = {OPEN, CLOSED, DRAFT, FILLED})
    private String active;

    @NotNull(message = "Company Id cannot be null")
    private Long companyId;

    @NotEmpty(message = "Skills cannot be empty")
    private List<Long> skills;
}
