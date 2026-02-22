package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.dto.request.SkillRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.SkillResponseDTO;
import com.caochung.recruitment.repository.SkillRepository;
import com.caochung.recruitment.service.SkillService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;

    @GetMapping("skills")
    public ResponseData<PaginationResponseDTO> getSkills(
            @Filter Specification<Skill> specification,
            Pageable pageable) {
        return ResponseData.success(this.skillService.getSkills(specification, pageable), SuccessCode.GET_SUCCESS);
    }

    @PostMapping("skills")
    @PreAuthorize(SecurityConstant.SKILL_CREATE)
    public ResponseData<SkillResponseDTO> createSkill(@Valid @RequestBody SkillRequestDTO skillRequestDTO) {
        return ResponseData.success(this.skillService.createSkill(skillRequestDTO), SuccessCode.CREATED_SUCCESS);
    }

    @GetMapping("/skills/{id}")
    public ResponseData<SkillResponseDTO> getSkillById(@PathVariable Long id) {
        return ResponseData.success(this.skillService.getSkillById(id), SuccessCode.GET_SUCCESS);
    }

    @PutMapping("skills/{id}")
    @PreAuthorize(SecurityConstant.SKILL_UPDATE)
    public ResponseData<?> updateSkill(@PathVariable Long id, @Valid @RequestBody SkillRequestDTO skill) {
        this.skillService.updateSkill(id, skill);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @DeleteMapping("skills/{id}")
    @PreAuthorize(SecurityConstant.SKILL_DELETE)
    public ResponseData<?> deleteSkill(@PathVariable Long id) {
        this.skillService.deleteSkill(id);
        return  ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
