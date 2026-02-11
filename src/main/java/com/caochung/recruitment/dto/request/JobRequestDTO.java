package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.constant.LevelEnum;
import com.caochung.recruitment.util.annotation.JobStatus;
import com.caochung.recruitment.util.annotation.Level;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    private String name;

    @NotBlank(message = "Location cannot be empty")
    private String location;

    private Double salary;

    private Integer quantity;

    @Level(anyOf = {FRESHER, INTERN, JUNIOR, MIDDLE, SENIOR})
    private String level;

    private String description;

    private Instant startDate;

    private Instant endDate;

    @JobStatus (anyOf = {OPEN, CLOSED, DRAFT, FILLED})
    private String active;

    @NotNull(message = "Company Id cannot be null")
    private Long companyId;

    @NotEmpty(message = "Skills cannot be empty")
    private List<Long> skills;
}
