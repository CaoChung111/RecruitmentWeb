package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.UserRequestDTO;
import com.caochung.recruitment.dto.request.UserUpdateDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.CompanyRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    private CompanyRepository companyRepository;

    @Mapping(source = "companyId", target = "company")
    public abstract User toEntity(UserRequestDTO userRequestDTO);

    public abstract UserResponseDTO toDto(User user);

    public abstract List<UserResponseDTO> toDto(List<User> userList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "companyId", target = "company")
    public abstract void fromUpdate(UserUpdateDTO userUpdateDTO, @MappingTarget User user);

    public abstract UserResponseDTO.CompanyResponseDTO toDtoUser(Company company);

    protected Company mapCompany(Long id){
        if(id==null){
            return null;
        }
        return companyRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.COMPANY_NOT_FOUND));
    }
}
