package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.dto.request.PermissionRequestDTO;
import com.caochung.recruitment.dto.response.PermissionResponseDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequestDTO permissionRequestDTO);

    PermissionResponseDTO toDTO(Permission permission);

    List<PermissionResponseDTO> toDTO(List<Permission> permissions);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void fromUpdate(PermissionRequestDTO permissionRequestDTO,@MappingTarget Permission permission);
}
