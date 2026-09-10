package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.dto.request.ResumeRequestDTO;
import com.caochung.recruitment.dto.request.ResumeUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.ResumeResponseDTO;
import com.caochung.recruitment.dto.response.ResumeDetailResponseDTO;
import com.caochung.recruitment.service.ResumeDetailService;
import com.caochung.recruitment.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Resume Management", description = "APIs for managing user resumes.")
public class ResumeController {
    private final ResumeService resumeService;
    private final ResumeDetailService resumeDetailService;

    @Operation(summary = "Get all resumes", description = "Retrieves a paginated list of all resumes, with optional filtering. Requires 'RESUME_VIEW_ALL' permission.")
    @GetMapping("/resumes")
    @PreAuthorize(SecurityConstant.RESUME_VIEW_ALL)
    public ResponseData<PaginationResponseDTO> getResumes(
            @Filter Specification<Resume> specification,
            Pageable pageable){
        PaginationResponseDTO paginationResponseDTO = this.resumeService.getResumes(specification,pageable);
        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Create a new resume", description = "Creates a new resume entry for the authenticated user. Requires 'RESUME_CREATE' permission.")
    @PostMapping(value = "/resumes")
    @PreAuthorize(SecurityConstant.RESUME_CREATE)
    public ResponseData<ResumeResponseDTO> createResume(@Valid @RequestBody ResumeRequestDTO resumeRequestDTO){
        ResumeResponseDTO resumeResponseDTO = this.resumeService.submitResume(resumeRequestDTO);
        return ResponseData.success(resumeResponseDTO, SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Update an existing resume", description = "Modifies the details of an existing resume identified by its ID. Requires 'RESUME_UPDATE' permission.")
    @PatchMapping("/resumes/{id}")
    @PreAuthorize(SecurityConstant.RESUME_UPDATE)
    public ResponseData<?> updateResume(@PathVariable Long id, @Valid @RequestBody ResumeUpdateDTO resumeUpdateDTO){
        this.resumeService.updateResume(id, resumeUpdateDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a resume", description = "Removes a resume permanently using its unique identifier. Requires 'RESUME_DELETE' permission.")
    @DeleteMapping("/resumes/{id}")
    @PreAuthorize(SecurityConstant.RESUME_DELETE)
    public ResponseData<?> deleteResume(@PathVariable Long id){
        this.resumeService.deleteResume(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }

    @Operation(summary = "Get resume by ID", description = "Fetches detailed information for a specific resume using its unique identifier. Requires 'RESUME_VIEW_DETAIL' permission.")
    @GetMapping("/resumes/{id}")
    @PreAuthorize(SecurityConstant.RESUME_VIEW_DETAIL)
    public ResponseData<ResumeResponseDTO> getResumeById(@PathVariable Long id){
        ResumeResponseDTO resumeResponseDTO = this.resumeService.getResumeById(id);
        return ResponseData.success(resumeResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get parsed resume detail", description = "Retrieves AI-parsed structured details of a resume. Requires 'RESUME_VIEW_DETAIL' permission.")
    @GetMapping("/resumes/{id}/parsed-detail")
    @PreAuthorize(SecurityConstant.RESUME_VIEW_DETAIL)
    public ResponseData<ResumeDetailResponseDTO> getResumeParsedDetail(@PathVariable Long id){
        ResumeDetailResponseDTO dto = this.resumeDetailService.getParsedResumeDetail(id);
        return ResponseData.success(dto, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get resumes by current user", description = "Retrieves a paginated list of resumes belonging to the currently authenticated user. Requires 'RESUME_VIEW_OWN' permission.")
    @GetMapping("/resumes/by-user")
    @PreAuthorize(SecurityConstant.RESUME_VIEW_OWN)
    public ResponseData<PaginationResponseDTO> getResumesByUser(Pageable pageable, @RequestParam(required = false) Integer size){
        if (size != null && size < 1) {
            throw new IllegalArgumentException("Page size must not be less than one");
        }
        PaginationResponseDTO responseDTO = this.resumeService.getResumeByUser(pageable);
        return ResponseData.success(responseDTO, SuccessCode.GET_SUCCESS);
    }

//    @GetMapping("/resumes/by-company")
//    @PreAuthorize(SecurityConstant.RESUME_VIEW_COMPANY)
//    public ResponseData<PaginationResponseDTO> getResumesByCompany(
//            @Filter Specification<Resume> specification,
//            Pageable pageable){
//        PaginationResponseDTO paginationResponseDTO = this.resumeService.getResumeByCompany(specification,pageable);
//        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
//    }
}
