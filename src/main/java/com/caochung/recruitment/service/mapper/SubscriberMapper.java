package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.dto.request.SubscriberRequestDTO;
import com.caochung.recruitment.dto.response.SubscriberResponseDTO;
import com.caochung.recruitment.repository.SkillRepository;
import com.caochung.recruitment.repository.SubscriberRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class SubscriberMapper {
    @Autowired
    private SkillRepository skillRepository;

    public abstract Subscriber toEntity(SubscriberRequestDTO subscriberRequestDTO);

    public abstract SubscriberResponseDTO toDTO(Subscriber subscriber);

    public abstract List<SubscriberResponseDTO> toDTO(List<Subscriber> subscribers);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void fromUpdate(SubscriberRequestDTO subscriberRequestDTO, @MappingTarget Subscriber subscriber);

    protected List<Skill> mapSkills(List<Long> skills) {
        if (skills == null || skills.isEmpty()) {
            return null;
        }
        return this.skillRepository.findAllById(skills);
    }
}
