package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.dto.request.JobRequestDTO;
import com.caochung.recruitment.dto.response.JobResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.service.JobService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Job Management", description = "APIs for managing job postings and listings.")
public class JobController {
    private final JobService jobService;

    @Operation(summary = "Get all jobs", description = "Retrieves a paginated list of all available job postings, with optional filtering. Accessible to all authenticated users.")
    @GetMapping("jobs")
    public ResponseData<PaginationResponseDTO> getJobs(
            @Filter Specification<Job> specification,
            Pageable pageable) {
        return ResponseData.success(this.jobService.getJobs(specification, pageable, false), SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get jobs by company", description = "Retrieves a paginated list of job postings associated with the current user's company, with optional filtering. Requires authentication.")
    @GetMapping("jobs/company")
    public ResponseData<PaginationResponseDTO> getJobsCompany(
            @Filter Specification<Job> specification,
            Pageable pageable) {
        return ResponseData.success(this.jobService.getJobs(specification, pageable, true), SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get job by ID", description = "Fetches detailed information for a specific job posting using its unique identifier. Accessible to all authenticated users.")
    @GetMapping("/jobs/{id}")
    public ResponseData<JobResponseDTO> getJobById(@PathVariable Long id) {
        return ResponseData.success(this.jobService.getJobById(id), SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Create a new job posting", description = "Creates a new job posting with the provided details. Requires 'JOB_CREATE' permission.")
    @PostMapping("jobs")
    @PreAuthorize(SecurityConstant.JOB_CREATE)
    public ResponseData<JobResponseDTO> createJob(@Valid @RequestBody JobRequestDTO job) {
        return ResponseData.success(this.jobService.createJob(job), SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Update an existing job posting", description = "Modifies the details of an existing job posting identified by its ID. Requires 'JOB_UPDATE' permission.")
    @PutMapping("jobs/{id}")
    @PreAuthorize(SecurityConstant.JOB_UPDATE)
    public ResponseData<?> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequestDTO job) {
        this.jobService.updateJob(id, job);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a job posting", description = "Removes a job posting permanently using its unique identifier. Requires 'JOB_DELETE' permission.")
    @DeleteMapping("jobs/{id}")
    @PreAuthorize(SecurityConstant.JOB_DELETE)
    public ResponseData<?> deleteJob(@PathVariable Long id) {
        this.jobService.deleteJob(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
