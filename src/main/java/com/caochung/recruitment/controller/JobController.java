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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @GetMapping("jobs")
    public ResponseData<PaginationResponseDTO> getJobs(
            @Filter Specification<Job> specification,
            Pageable pageable) {
        return ResponseData.success(this.jobService.getJobs(specification, pageable), SuccessCode.GET_SUCCESS);
    }

    @GetMapping("/jobs/{id}")
    public ResponseData<JobResponseDTO> getJobById(@PathVariable Long id) {
        return ResponseData.success(this.jobService.getJobById(id), SuccessCode.GET_SUCCESS);
    }

    @PostMapping("jobs")
    @PreAuthorize(SecurityConstant.JOB_CREATE)
    public ResponseData<JobResponseDTO> createJob(@Valid @RequestBody JobRequestDTO job) {
        return ResponseData.success(this.jobService.createJob(job), SuccessCode.CREATED_SUCCESS);
    }

    @PutMapping("jobs/{id}")
    @PreAuthorize(SecurityConstant.JOB_UPDATE)
    public ResponseData<?> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequestDTO job) {
        this.jobService.updateJob(id, job);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @DeleteMapping("jobs/{id}")
    @PreAuthorize(SecurityConstant.JOB_DELETE)
    public ResponseData<?> deleteJob(@PathVariable Long id) {
        this.jobService.deleteJob(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
