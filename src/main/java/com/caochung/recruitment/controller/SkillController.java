package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.dto.request.SkillRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.SkillResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Skill Management", description = "APIs for managing skills.")
public class SkillController {
    private final SkillService skillService;

    @Operation(summary = "Get all skills", description = "Retrieves a paginated list of all skills, with optional filtering. Accessible to all authenticated users.")
    @GetMapping("skills")
    public ResponseData<PaginationResponseDTO> getSkills(
            @Filter Specification<Skill> specification,
            Pageable pageable) {
        return ResponseData.success(this.skillService.getSkills(specification, pageable), SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Create a new skill", description = "Creates a new skill entry in the system. Requires 'SKILL_CREATE' permission.")
    @PostMapping("skills")
    @PreAuthorize(SecurityConstant.SKILL_CREATE)
    public ResponseData<SkillResponseDTO> createSkill(@Valid @RequestBody SkillRequestDTO skillRequestDTO) {
        return ResponseData.success(this.skillService.createSkill(skillRequestDTO), SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Get skill by ID", description = "Fetches detailed information for a specific skill using its unique identifier. Accessible to all authenticated users.")
    @GetMapping("/skills/{id}")
    public ResponseData<SkillResponseDTO> getSkillById(@PathVariable Long id) {
        return ResponseData.success(this.skillService.getSkillById(id), SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Update an existing skill", description = "Modifies the details of an existing skill identified by its ID. Requires 'SKILL_UPDATE' permission.")
    @PutMapping("skills/{id}")
    @PreAuthorize(SecurityConstant.SKILL_UPDATE)
    public ResponseData<?> updateSkill(@PathVariable Long id, @Valid @RequestBody SkillRequestDTO skill) {
        this.skillService.updateSkill(id, skill);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a skill", description = "Removes a skill permanently using its unique identifier. Requires 'SKILL_DELETE' permission.")
    @DeleteMapping("skills/{id}")
    @PreAuthorize(SecurityConstant.SKILL_DELETE)
    public ResponseData<?> deleteSkill(@PathVariable Long id) {
        this.skillService.deleteSkill(id);
        return  ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
