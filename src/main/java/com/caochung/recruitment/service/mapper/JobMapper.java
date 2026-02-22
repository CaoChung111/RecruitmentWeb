package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.dto.request.JobRequestDTO;
import com.caochung.recruitment.dto.request.SkillRequestDTO;
import com.caochung.recruitment.dto.response.JobResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.SkillRepository;
import com.caochung.recruitment.service.JobService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class JobMapper {
    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Mapping(source = "companyId", target = "company")
    public abstract Job toJob(JobRequestDTO jobRequestDTO);

    public abstract JobResponseDTO toDto(Job job);

    public abstract List<JobResponseDTO> toDto(List<Job> jobs);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract Job fromUpdateJob(JobRequestDTO jobRequestDTO,@MappingTarget Job job);

    public abstract JobResponseDTO.JobCompany toDtoCompany(Company company);

    protected List<Skill> mapSkills(List<Long> skills) {
        if (skills == null || skills.isEmpty()) {
            return null;
        }
        return this.skillRepository.findAllById(skills);
    }

    protected Company mapCompany(Long id){
        if (id == null) {
            return null;
        }else {
            return companyRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.COMPANY_NOT_FOUND));
        }
    }
}
