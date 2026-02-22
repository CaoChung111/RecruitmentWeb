package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.RoleRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.RoleResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.RoleRepository;
import com.caochung.recruitment.service.RoleService;
import com.caochung.recruitment.service.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO) {
        if(roleRepository.existsByName(roleRequestDTO.getName())){
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        Role role = roleMapper.toRole(roleRequestDTO);
        Role savedRole = roleRepository.save(role);
        return roleMapper.toDTO(savedRole);
    }

    @Override
    public PaginationResponseDTO getRoles(Specification<Role> specification, Pageable pageable) {
        Page<Role> rolePage = roleRepository.findAll(specification, pageable);
        List<RoleResponseDTO> roleResponseDTOS = roleMapper.toDTO(rolePage.getContent());

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPages(rolePage.getTotalPages())
                .totalItems(rolePage.getTotalElements())
                .build();
        return new PaginationResponseDTO(meta, roleResponseDTOS);
    }

    @Override
    public RoleResponseDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        return roleMapper.toDTO(role);
    }

    @Override
    public void updateRole(Long id, RoleRequestDTO roleRequestDTO) {
        Role role = roleRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        if(!role.getName().equals(roleRequestDTO.getName()) && roleRepository.existsByName(roleRequestDTO.getName())){
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        this.roleMapper.fromUpdate(roleRequestDTO, role);
        this.roleRepository.save(role);
    }

    @Override
    public void deleteRole(Long id) {
        this.roleRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        this.roleRepository.deleteById(id);
    }
}
