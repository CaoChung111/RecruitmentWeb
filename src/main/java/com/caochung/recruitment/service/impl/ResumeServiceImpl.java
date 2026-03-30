package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.ResumeRequestDTO;
import com.caochung.recruitment.dto.request.ResumeUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResumeResponseDTO;
import com.caochung.recruitment.event.ResumeStatusUpdateEvent;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.ResumeRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.CloudinaryService;
import com.caochung.recruitment.service.ResumeService;
import com.caochung.recruitment.service.mapper.ResumeMapper;
import com.caochung.recruitment.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.EventListener;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional
    public ResumeResponseDTO submitResume(ResumeRequestDTO resumeRequestDTO) {
        Job job = jobRepository.findById(resumeRequestDTO.getJobId()).orElseThrow(
                () -> new AppException(ErrorCode.JOB_NOT_FOUND));
        if(job.getActive().equals(JobStatusEnum.CLOSED) || job.getActive().equals(JobStatusEnum.DRAFT)){
            throw new AppException(ErrorCode.JOB_INACTIVE);
        }
        if (job.getCompany().getStatus().equals(CompanyStatusEnum.INACTIVE)) {
            throw new AppException(ErrorCode.COMPANY_INACTIVE);
        }
        if(resumeRepository.existsByJob_IdAndEmail(resumeRequestDTO.getJobId(), resumeRequestDTO.getEmail())){
            throw new AppException(ErrorCode.ALREADY_APPLIED);
        }
        Resume resume = this.resumeMapper.toResume(resumeRequestDTO);
        return resumeMapper.toDTO(this.resumeRepository.save(resume));
    }

    @Override
    @Transactional
    public void updateResume(Long id, ResumeUpdateDTO resumeUpdateDTO) {
        Resume resume = resumeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        resumeMapper.fromUpdate(resumeUpdateDTO, resume);
        ResumeStatusUpdateEvent event = ResumeStatusUpdateEvent.builder()
                .emailTo(resume.getEmail())
                .username(resume.getUser().getName())
                .jobName(resume.getJob().getName())
                .companyName(resume.getJob().getCompany().getName())
                .status(resume.getStatus())
                .build();
        publisher.publishEvent(event);
    }

    @Override
    @Transactional
    public void deleteResume(Long id) {
        Resume resume = resumeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(()->new AppException(ErrorCode.UNAUTHENTICATED));
        ResumeStatusUpdateEvent event = ResumeStatusUpdateEvent.builder()
                .emailTo(resume.getEmail())
                .username(resume.getUser().getName())
                .jobName(resume.getJob().getName())
                .companyName(resume.getJob().getCompany().getName())
                .build();
        if(resume.getEmail().equals(email)){
            resume.setStatus(ResumeStatusEnum.WITHDRAWN);
            event.setStatus(ResumeStatusEnum.WITHDRAWN);
        }else {
            resume.setStatus(ResumeStatusEnum.SYSTEM_CANCEL);
            event.setStatus(ResumeStatusEnum.SYSTEM_CANCEL);
        }
        publisher.publishEvent(event);
    }

    @Override
    public ResumeResponseDTO getResumeById(Long id) {
        Resume resume = resumeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        return resumeMapper.toDTO(resume);
    }

    @Override
    public PaginationResponseDTO getResumeByUser(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHENTICATED));

        Specification<Resume> spec = (root, query, cb) ->
                cb.equal(root.get("user").get("email"), email);
        Page<Resume> resumePage = this.resumeRepository.findAll(spec, pageable);
        List<ResumeResponseDTO>  resumeResponseDTOS = this.resumeMapper.toDTO(resumePage.getContent());
        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalItems(resumePage.getTotalElements())
                .totalPages(resumePage.getTotalPages())
                .build();
        return new PaginationResponseDTO(meta, resumeResponseDTOS);
    }

    @Override
    public PaginationResponseDTO getResumes(Specification<Resume> specification, Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(()->new AppException(ErrorCode.UNAUTHENTICATED));

        User user = this.userRepository.findByEmail(email).orElseThrow(()->
                new AppException(ErrorCode.USER_NOT_FOUND));

        Specification<Resume> finalSpec = specification;
        if (user.getCompany() != null){
            Specification<Resume> spec = (root, query, cb) ->
                    cb.equal(root.get("job").get("company").get("id"), user.getCompany().getId());
            finalSpec = (specification == null) ? spec : specification.and(spec);
        }

        Page<Resume> resumePage = this.resumeRepository.findAll(finalSpec, pageable);
        List<ResumeResponseDTO> resumeResponseDTOS = this.resumeMapper.toDTO(resumePage.getContent());

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalItems(resumePage.getTotalElements())
                .totalPages(resumePage.getTotalPages())
                .build();
        return new PaginationResponseDTO(meta, resumeResponseDTOS);
    }
}
