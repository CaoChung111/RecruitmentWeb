package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.RoleRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.RoleResponseDTO;
import com.caochung.recruitment.service.RoleService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/roles")
    @PreAuthorize(SecurityConstant.ROLE_CREATE)
    public ResponseData<RoleResponseDTO> createRole(@Valid @RequestBody RoleRequestDTO roleRequestDTO) {
        RoleResponseDTO responseDTO = this.roleService.createRole(roleRequestDTO);
        return ResponseData.success(responseDTO, SuccessCode.CREATED_SUCCESS);
    }

    @GetMapping("/roles")
    @PreAuthorize(SecurityConstant.ROLE_VIEW_ALL)
    public ResponseData<PaginationResponseDTO> getRoles(
            @Filter Specification<Role> specification,
            Pageable pageable){
        PaginationResponseDTO  paginationResponseDTO = this.roleService.getRoles(specification,pageable);
        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize(SecurityConstant.ROLE_VIEW_DETAIL)
    public ResponseData<RoleResponseDTO> getRoleById(@PathVariable Long id){
        RoleResponseDTO responseDTO = this.roleService.getRoleById(id);
        return ResponseData.success(responseDTO, SuccessCode.GET_SUCCESS);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize(SecurityConstant.ROLE_UPDATE)
    public ResponseData<?> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequestDTO roleRequestDTO){
        this.roleService.updateRole(id, roleRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize(SecurityConstant.ROLE_DELETE)
    public ResponseData<?> deleteRole(@PathVariable Long id){
        this.roleService.deleteRole(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
