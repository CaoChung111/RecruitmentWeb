package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.RoleRequestDTO;
import com.caochung.recruitment.dto.response.LoginResponseDTO;
import com.caochung.recruitment.dto.response.RoleResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.PermissionRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class RoleMapper {
    @Autowired
    private PermissionRepository permissionRepository;

    public abstract Role toRole(RoleRequestDTO roleRequestDTO);

    public abstract RoleResponseDTO toDTO(Role role);

    public abstract RoleResponseDTO.PermissionRole toDTO(Permission permission);

    public abstract List<RoleResponseDTO> toDTO(List<Role> roles);

    public abstract void fromUpdate(RoleRequestDTO roleRequestDTO,@MappingTarget Role role);

    protected Set<Permission> mapRole(List<Long> permissionIds){
        if(permissionIds == null || permissionIds.isEmpty()){
            return null;
        }
        return new HashSet<>(permissionRepository.findAllById(permissionIds));
    }
}
