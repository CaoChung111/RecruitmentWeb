package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.dto.request.SkillRequestDTO;
import com.caochung.recruitment.dto.response.SkillResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    Skill toSkill(SkillRequestDTO skillRequestDTO);
    SkillResponseDTO toDto(Skill skill);
    List<SkillResponseDTO> toDto(List<Skill> skillList);
}
