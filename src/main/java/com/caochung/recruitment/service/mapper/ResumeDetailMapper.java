package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.ai.dto.ParsedCvDTO;
import com.caochung.recruitment.domain.ResumeDetail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ResumeDetailMapper {
    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resume", ignore = true)
    @Mapping(target = "analysisStatus", ignore = true)
    @Mapping(target = "skills", source = "skills", qualifiedByName = "objectToJson")
    @Mapping(target = "experiences", source = "experiences", qualifiedByName = "objectToJson")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateResumeDetail(ParsedCvDTO parsedCvDTO, @MappingTarget ResumeDetail resumeDetail);

    @Named("objectToJson")
    protected String objectToJson(Object object) {
        if (object == null) return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}
