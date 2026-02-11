package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.dto.request.ResumeRequestDTO;
import com.caochung.recruitment.dto.request.ResumeUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.ResumeResponseDTO;
import com.caochung.recruitment.service.ResumeService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;

    @GetMapping("/resumes")
    public ResponseData<PaginationResponseDTO> getResumes(
            @Filter Specification<Resume> specification,
            Pageable pageable){
        PaginationResponseDTO paginationResponseDTO = this.resumeService.getResumes(specification,pageable);
        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @PostMapping(value = "/resumes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<ResumeResponseDTO> createResume(@Valid @RequestPart("resume") ResumeRequestDTO resumeRequestDTO,
                                                        @RequestPart("file") MultipartFile cv){
        ResumeResponseDTO resumeResponseDTO = this.resumeService.createResume(resumeRequestDTO, cv);
        return ResponseData.success(resumeResponseDTO, SuccessCode.CREATED_SUCCESS);
    }

    @PatchMapping("/resumes/{id}")
    public ResponseData<?> updateResume(@PathVariable Long id, @Valid @RequestBody ResumeUpdateDTO resumeUpdateDTO){
        this.resumeService.updateResume(id, resumeUpdateDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @DeleteMapping("/resumes/{id}")
    public ResponseData<?> deleteResume(@PathVariable Long id){
        this.resumeService.deleteResume(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }

    @GetMapping("/resumes/{id}")
    public ResponseData<ResumeResponseDTO> getResume(@PathVariable Long id){
        ResumeResponseDTO resumeResponseDTO = this.resumeService.getResumeById(id);
        return ResponseData.success(resumeResponseDTO, SuccessCode.GET_SUCCESS);
    }
}
