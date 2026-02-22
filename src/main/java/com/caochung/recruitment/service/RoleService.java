package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.RoleRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.RoleResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface RoleService {
    RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO);

    PaginationResponseDTO getRoles(Specification<Role> specification, Pageable pageable);

    RoleResponseDTO getRoleById(Long id);

    void updateRole(Long id, RoleRequestDTO roleRequestDTO);

    void deleteRole(Long id);
}
