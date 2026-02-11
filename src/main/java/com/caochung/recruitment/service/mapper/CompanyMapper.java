package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    Company toEntity(CompanyRequestDTO companyRequestDTO);

    CompanyResponseDTO toDto(Company company);

    List<CompanyResponseDTO> toDto(List<Company> companyList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void fromUpdateDto(CompanyRequestDTO companyRequestDTO, @MappingTarget Company company);
}
