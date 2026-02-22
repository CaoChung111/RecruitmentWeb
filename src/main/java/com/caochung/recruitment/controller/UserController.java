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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/users")
    @PreAuthorize(SecurityConstant.USER_CREATE)
    public ResponseData<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO user) {
        UserResponseDTO userResponseDTO=this.userService.createUser(user);
        return ResponseData.success(userResponseDTO ,SuccessCode.CREATED_SUCCESS);
    }

    @GetMapping("/users")
    @PreAuthorize(SecurityConstant.USER_VIEW_ALL)
    public ResponseData<PaginationResponseDTO> getAllUsers(
            @Filter Specification<User> specification, Pageable pageable) {
        PaginationResponseDTO paginationResponseDTO = this.userService.getAllUsers(specification, pageable);
        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize(SecurityConstant.USER_VIEW_DETAIL)
    public ResponseData<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO userResponseDTO = this.userService.getUserById(id);
        return ResponseData.success(userResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize(SecurityConstant.USER_UPDATE)
    public ResponseData<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO user) {
        this.userService.updateUser(id, user);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize(SecurityConstant.USER_DELETE)
    public ResponseData<?> deleteUser(@PathVariable Long id) {
        this.userService.deleteUser(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }

}
