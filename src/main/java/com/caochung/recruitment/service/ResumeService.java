package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.ResumeRequestDTO;
import com.caochung.recruitment.dto.request.ResumeUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResumeResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ResumeService {
    ResumeResponseDTO createResume(ResumeRequestDTO resumeRequestDTO, MultipartFile file);

    void updateResume(Long id, ResumeUpdateDTO resumeUpdateDTO);

    void deleteResume(Long id);

    PaginationResponseDTO getResumes(Specification<Resume> specification, Pageable pageable);

    ResumeResponseDTO getResumeById(Long id);

    PaginationResponseDTO getResumeByUser(Pageable pageable);

    PaginationResponseDTO getResumeByCompany(Specification<Resume> specification ,Pageable pageable);

}
