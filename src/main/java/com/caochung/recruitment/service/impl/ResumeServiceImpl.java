package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.dto.request.ResumeRequestDTO;
import com.caochung.recruitment.dto.request.ResumeUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResumeResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.ResumeRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.CloudinaryService;
import com.caochung.recruitment.service.ResumeService;
import com.caochung.recruitment.service.mapper.ResumeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public ResumeResponseDTO createResume(ResumeRequestDTO resumeRequestDTO, MultipartFile cv) {
        resumeRequestDTO.setUrl(cloudinaryService.uploadFile(cv));
        Resume resume = this.resumeMapper.toResume(resumeRequestDTO);
        return resumeMapper.toDTO(this.resumeRepository.save(resume));
    }

    @Override
    public void updateResume(Long id, ResumeUpdateDTO resumeUpdateDTO) {
        Resume resume = resumeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        resumeMapper.fromUpdate(resumeUpdateDTO, resume);
        this.resumeRepository.save(resume);
    }

    @Override
    public void deleteResume(Long id) {
        Resume resume = resumeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        this.resumeRepository.delete(resume);
    }

    @Override
    public PaginationResponseDTO getResumes(Specification<Resume> specification, Pageable pageable) {
        Page<Resume> resumePage = this.resumeRepository.findAll(specification, pageable);
        List<ResumeResponseDTO> resumeResponseDTOS = this.resumeMapper.toDTO(resumePage.getContent());

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalItems(resumePage.getTotalElements())
                .totalPages(resumePage.getTotalPages())
                .build();
        return new PaginationResponseDTO(meta, resumeResponseDTOS);
    }

    @Override
    public ResumeResponseDTO getResumeById(Long id) {
        Resume resume = resumeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        return resumeMapper.toDTO(resume);
    }
}
