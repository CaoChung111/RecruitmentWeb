package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.dto.request.PermissionRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.PermissionResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.repository.PermissionRepository;
import com.caochung.recruitment.service.PermissionService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.FilteredEndpoint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping("/permissions")
    @PreAuthorize(SecurityConstant.PERMISSION_CREATE)
    public ResponseData<PermissionResponseDTO> createPermission(@Valid @RequestBody PermissionRequestDTO permissionRequestDTO) {
        PermissionResponseDTO responseDTO = this.permissionService.createPermission(permissionRequestDTO);
        return ResponseData.success(responseDTO, SuccessCode.CREATED_SUCCESS);
    }

    @GetMapping("/permissions")
    @PreAuthorize(SecurityConstant.PERMISSION_VIEW_ALL)
    public ResponseData<PaginationResponseDTO> getPermissions(
            @Filter Specification<Permission> specification,
            Pageable pageable){
        PaginationResponseDTO  paginationResponseDTO = this.permissionService.getPermissions(specification,pageable);
        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @GetMapping("/permissions/{id}")
    @PreAuthorize(SecurityConstant.PERMISSION_VIEW_DETAIL)
    public ResponseData<PermissionResponseDTO> getPermissionById(@PathVariable Long id){
        PermissionResponseDTO responseDTO = this.permissionService.getPermissionById(id);
        return ResponseData.success(responseDTO, SuccessCode.GET_SUCCESS);
    }

    @PutMapping("/permissions/{id}")
    @PreAuthorize(SecurityConstant.PERMISSION_UPDATE)
    public ResponseData<?> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionRequestDTO permissionRequestDTO){
        this.permissionService.updatePermission(id, permissionRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @DeleteMapping("/permissions/{id}")
    @PreAuthorize(SecurityConstant.PERMISSION_DELETE)
    public ResponseData<?> deletePermission(@PathVariable Long id){
        this.permissionService.deletePermission(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
