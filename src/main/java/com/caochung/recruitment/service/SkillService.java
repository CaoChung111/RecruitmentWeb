package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.dto.request.SkillRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.SkillResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface SkillService {
    SkillResponseDTO createSkill(SkillRequestDTO skillRequestDTO);

    PaginationResponseDTO getSkills(Specification<Skill> specification, Pageable pageable);

    void updateSkill(Long id, SkillRequestDTO skillRequestDTO);

    void deleteSkill(Long id);
}
