package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.dto.request.SkillRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.SkillResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.SkillRepository;
import com.caochung.recruitment.repository.SubscriberRepository;
import com.caochung.recruitment.service.SkillService;
import com.caochung.recruitment.service.mapper.SkillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillServiceImpl implements SkillService {
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final SubscriberRepository subscriberRepository;
    private final SkillMapper skillMapper;

    @Override
    @Transactional
    public SkillResponseDTO createSkill(SkillRequestDTO skillRequestDTO) {
        if(skillRepository.existsByName(skillRequestDTO.getName())){
            throw new AppException(ErrorCode.SKILL_EXISTED);
        }
        Skill skill = skillMapper.toSkill(skillRequestDTO);
        Skill savedSkill = skillRepository.save(skill);
        return skillMapper.toDto(savedSkill);
    }

    @Override
    public PaginationResponseDTO getSkills(Specification<Skill> specification, Pageable pageable) {
        Page<Skill> skillPage = this.skillRepository.findAll(specification, pageable);
        List<SkillResponseDTO> skillResponseDTOS = this.skillMapper.toDto(skillPage.getContent());
        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(skillPage.getTotalPages())
                .totalItems(skillPage.getTotalElements())
                .build();
        return new PaginationResponseDTO(meta, skillResponseDTOS);
    }

    @Override
    @Cacheable(value = "skill_detail", key = "#id")
    public SkillResponseDTO getSkillById(Long id) {
        Skill skill = this.skillRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_FOUND));
        return skillMapper.toDto(skill);
    }

    @Override
    @Transactional
    @CacheEvict(value = "skill_detail", key = "#id")
    public void updateSkill(Long id, SkillRequestDTO skillRequestDTO) {
        Skill skill = this.skillRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.SKILL_NOT_FOUND));
        if(!skill.getName().equals(skillRequestDTO.getName()) && skillRepository.existsByName(skillRequestDTO.getName())){
            throw new AppException(ErrorCode.SKILL_EXISTED);
        }
        skill.setName(skillRequestDTO.getName());
    }

    @Override
    @Transactional
    @CacheEvict(value = "skill_detail", key = "#id")
    public void deleteSkill(Long id) {
        Skill skill = this.skillRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.SKILL_NOT_FOUND));

        List<Job> jobs = this.jobRepository.findAllBySkillsContaining(skill);
        if (!jobs.isEmpty()) {
            throw new AppException(ErrorCode.SKILL_HAS_USED);
        }

        List<Subscriber> subscribers = this.subscriberRepository.findAllBySkillsContaining(skill);
        if (!subscribers.isEmpty()) {
            throw new AppException(ErrorCode.SKILL_HAS_USED);
        }
        this.skillRepository.delete(skill);
    }
}
