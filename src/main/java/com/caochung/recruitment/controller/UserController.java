package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.UserRequestDTO;
import com.caochung.recruitment.dto.request.UserUpdateDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.service.UserService;
import com.caochung.recruitment.service.impl.UserServiceImpl;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing user accounts and profiles.")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Create a new user", description = "Registers a new user account with the provided details. Requires 'USER_CREATE' permission.")
    @PostMapping("/users")
    @PreAuthorize(SecurityConstant.USER_CREATE)
    public ResponseData<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO user) {
        UserResponseDTO userResponseDTO=this.userService.createUser(user);
        return ResponseData.success(userResponseDTO ,SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users, with optional filtering. Requires 'USER_VIEW_ALL' permission.")
    @GetMapping("/users")
    @PreAuthorize(SecurityConstant.USER_VIEW_ALL)
    public ResponseData<PaginationResponseDTO> getAllUsers(
            @Filter Specification<User> specification, Pageable pageable) {
        PaginationResponseDTO paginationResponseDTO = this.userService.getAllUsers(specification, pageable);
        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get user by ID", description = "Fetches detailed information for a specific user using their unique identifier. Requires 'USER_VIEW_DETAIL' permission.")
    @GetMapping("/users/{id}")
    @PreAuthorize(SecurityConstant.USER_VIEW_DETAIL)
    public ResponseData<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO userResponseDTO = this.userService.getUserById(id);
        return ResponseData.success(userResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Update an existing user", description = "Modifies the details of an existing user identified by their ID. Requires 'USER_UPDATE' permission.")
    @PutMapping("/users/{id}")
    @PreAuthorize(SecurityConstant.USER_UPDATE)
    public ResponseData<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO user) {
        this.userService.updateUser(id, user);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a user", description = "Removes a user account permanently using their unique identifier. Requires 'USER_DELETE' permission.")
    @DeleteMapping("/users/{id}")
    @PreAuthorize(SecurityConstant.USER_DELETE)
    public ResponseData<?> deleteUser(@PathVariable Long id) {
        this.userService.deleteUser(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }

    @Operation(summary = "Get all disable users", description = "Retrieves a paginated list of all disable users. Requires 'USER_VIEW_DISABLE' permission.")
    @GetMapping("/users/trash")
    @PreAuthorize(SecurityConstant.USER_VIEW_DISABLE)
    public ResponseData<PaginationResponseDTO> getAllDisableUsers(Pageable pageable) {
        PaginationResponseDTO resultPagination= this.userService.getAllDisableUser(pageable);
        return ResponseData.success(resultPagination, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Restore a user", description = "Restore a user account. Requires 'USER_RESTORE' permission.")
    @PutMapping("/users/{id}/restore")
    @PreAuthorize(SecurityConstant.USER_RESTORE)
    public ResponseData<?> restoreUser(@PathVariable Long id) {
        this.userService.restoreUserById(id);
        return ResponseData.success(SuccessCode.RESTORE_SUCCESS);
    }

}
