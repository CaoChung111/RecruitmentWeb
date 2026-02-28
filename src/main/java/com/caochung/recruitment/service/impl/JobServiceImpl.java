package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.JobRequestDTO;
import com.caochung.recruitment.dto.response.JobResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.JobService;
import com.caochung.recruitment.service.mapper.JobMapper;
import com.caochung.recruitment.util.SecurityUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    @Override
    public JobResponseDTO createJob(JobRequestDTO jobRequestDTO) {
        if(this.jobRepository.existsByName(jobRequestDTO.getName())){
            throw new AppException(ErrorCode.JOB_EXISTED);
        }
        Job job = this.jobMapper.toJob(jobRequestDTO);
        return this.jobMapper.toDto(this.jobRepository.save(job));
    }

    @Override
    public PaginationResponseDTO getJobs(Specification<Job> specification, Pageable pageable, boolean isDashBoard) {
        if (isDashBoard) {
            String email = SecurityUtil.getCurrentUserLogin().isPresent()
                    ? SecurityUtil.getCurrentUserLogin().get() : null;
            User user = this.userRepository.findByEmail(email);
            if (user.getCompany() != null) {
                Specification<Job> spec = (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("company"), user.getCompany());
                specification = specification.and(spec);
            }
        }
        Page<Job> jobs = this.jobRepository.findAll(specification, pageable);
        List<JobResponseDTO> jobResponseDTOS = this.jobMapper.toDto(jobs.getContent());
        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(jobs.getTotalPages())
                .totalItems(jobs.getTotalElements())
                .build();
        return new PaginationResponseDTO(meta, jobResponseDTOS);
    }

    @Override
    public JobResponseDTO getJobById(Long id) {
        Job job = this.jobRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        return this.jobMapper.toDto(job);
    }

    @Override
    public void updateJob(Long id, JobRequestDTO jobRequestDTO) {
        Job job = this.jobRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        if(!job.getName().equals(jobRequestDTO.getName()) && this.jobRepository.existsByName(jobRequestDTO.getName())){
            throw new AppException(ErrorCode.JOB_EXISTED);
        }
        this.jobMapper.fromUpdateJob(jobRequestDTO, job);
        this.jobRepository.save(job);
    }

    @Override
    public void deleteJob(Long id) {
        Job job = this.jobRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        this.jobRepository.delete(job);
    }
}
