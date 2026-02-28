package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.RegisterDTO;
import com.caochung.recruitment.dto.request.UserRequestDTO;
import com.caochung.recruitment.dto.request.UserUpdateDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.RoleRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Mapping(source = "companyId", target = "company")
    public abstract User toEntity(UserRequestDTO userRequestDTO);

    public abstract UserResponseDTO toDto(User user);

    public abstract List<UserResponseDTO> toDto(List<User> userList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "companyId", target = "company")
    public abstract void fromUpdate(UserUpdateDTO userUpdateDTO, @MappingTarget User user);

    public abstract UserResponseDTO.CompanyResponseDTO toDtoUser(Company company);

    public abstract User toEntity(RegisterDTO registerDTO);

    protected Company mapCompany(Long id){
        if(id==null){
            return null;
        }
        return companyRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.COMPANY_NOT_FOUND));
    }

    protected Role mapRole(Long id){
        if(id==null){
            return null;
        }
        return roleRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.ROLE_NOT_FOUND));
    }

    @AfterMapping
    protected void mapCompany(UserUpdateDTO userUpdateDTO, @MappingTarget User user) {
        if (userUpdateDTO.getCompanyId() != null) {
            Company company = new Company();
            company.setId(userUpdateDTO.getCompanyId());
            user.setCompany(company);
        } else {
            // Nếu companyId gửi lên là null, ta chủ động set null cho user
            user.setCompany(null);
        }
    }
}
