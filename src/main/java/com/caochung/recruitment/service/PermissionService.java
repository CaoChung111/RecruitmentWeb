package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.dto.request.PermissionRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.PermissionResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PermissionService {
    PermissionResponseDTO createPermission(PermissionRequestDTO permissionRequestDTO);

    PaginationResponseDTO getPermissions(Specification<Permission> specification, Pageable  pageable);

    PermissionResponseDTO getPermissionById(Long id);

    void updatePermission(Long id, PermissionRequestDTO permissionRequestDTO);

    void deletePermission(Long id);
}
