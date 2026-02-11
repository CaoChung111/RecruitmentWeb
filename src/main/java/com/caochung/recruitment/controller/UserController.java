package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.UserRequestDTO;
import com.caochung.recruitment.dto.request.UserUpdateDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.service.impl.UserServiceImpl;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserServiceImpl userServiceImpl;

    public UserController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping("/users")
    private ResponseData<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO user) {
        UserResponseDTO userResponseDTO=this.userServiceImpl.createUser(user);
        return ResponseData.success(userResponseDTO ,SuccessCode.CREATED_SUCCESS);
    }

    @GetMapping("/users")
    private ResponseData<PaginationResponseDTO> getAllUsers(
            @Filter Specification<User> specification, Pageable pageable) {
        PaginationResponseDTO paginationResponseDTO = this.userServiceImpl.getAllUsers(specification, pageable);
        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @GetMapping("/users/{id}")
    private ResponseData<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO userResponseDTO = this.userServiceImpl.getUserById(id);
        return ResponseData.success(userResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @PutMapping("/users/{id}")
    public ResponseData<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO user) {
        this.userServiceImpl.updateUser(id, user);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @DeleteMapping("/users/{id}")
    public ResponseData<?> deleteUser(@PathVariable Long id) {
        this.userServiceImpl.deleteUser(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }

}
