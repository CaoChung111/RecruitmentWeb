package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.dto.request.JobRequestDTO;
import com.caochung.recruitment.dto.response.JobResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface JobService {
    JobResponseDTO createJob(JobRequestDTO jobRequestDTO);

    PaginationResponseDTO getJobs(Specification<Job> specification, Pageable pageable);

    void updateJob(Long id, JobRequestDTO jobRequestDTO);

    void deleteJob(Long id);
}
