package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.PermissionRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.PermissionResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.PermissionRepository;
import com.caochung.recruitment.repository.RoleRepository;
import com.caochung.recruitment.service.PermissionService;
import com.caochung.recruitment.service.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionMapper permissionMapper;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    public PermissionResponseDTO createPermission(PermissionRequestDTO permissionRequestDTO) {
        checkPermission(permissionRequestDTO);
        Permission permission = this.permissionMapper.toPermission(permissionRequestDTO);
        Permission savedPermission = this.permissionRepository.save(permission);
        return permissionMapper.toDTO(savedPermission);
    }

    @Override
    public PaginationResponseDTO getPermissions(Specification<Permission> specification, Pageable pageable) {
        Page<Permission> permissionPage = this.permissionRepository.findAll(specification, pageable);
        List<PermissionResponseDTO> permissionResponseDTOS = this.permissionMapper.toDTO(permissionPage.getContent());

        PaginationResponseDTO.Meta meta= PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(permissionPage.getTotalPages())
                .totalItems(permissionPage.getTotalElements())
                .build();
        return new PaginationResponseDTO(meta, permissionResponseDTOS);
    }

    @Override
    public PermissionResponseDTO getPermissionById(Long id) {
        Permission permission = this.permissionRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.PERMISSION_NOT_FOUND)
        );
        return permissionMapper.toDTO(permission);
    }

    @Override
    public void updatePermission(Long id, PermissionRequestDTO permissionRequestDTO) {
        Permission permission = this.permissionRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        if( permissionRepository.existsByApiPathAndMethodAndModuleAndIdNot(permissionRequestDTO.getApiPath(),
                permissionRequestDTO.getMethod(), permissionRequestDTO.getModule(), id)){
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }
        this.permissionMapper.fromUpdate(permissionRequestDTO,permission);
        this.permissionRepository.save(permission);
    }

    @Override
    public void deletePermission(Long id) {
        Permission permission = this.permissionRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        List<Role> roles = this.roleRepository.findAllByPermissionsContains(permission);
        for (Role role : roles) {
            role.getPermissions().remove(permission);
        }
        this.permissionRepository.deleteById(id);
    }

    private void checkPermission(PermissionRequestDTO permissionRequestDTO) {
        if( permissionRepository.existsByApiPathAndMethodAndModule(permissionRequestDTO.getApiPath(),
                permissionRequestDTO.getMethod(), permissionRequestDTO.getModule())){
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }
    }
}
