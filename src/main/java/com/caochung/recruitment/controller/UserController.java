package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.domain.dto.request.UserRequestDTO;
import com.caochung.recruitment.domain.dto.request.UserUpdateDTO;
import com.caochung.recruitment.domain.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.domain.dto.response.ResponseData;
import com.caochung.recruitment.domain.dto.response.ResultPaginationDTO;
import com.caochung.recruitment.domain.dto.response.UserResponseDTO;
import com.caochung.recruitment.service.UserService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    private ResponseData<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO user) {
        UserResponseDTO userResponseDTO=this.userService.createUser(user);
        return ResponseData.success(userResponseDTO ,SuccessCode.CREATED_SUCCESS);
    }

    @GetMapping("/users")
    private ResponseData<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> specification, Pageable pageable) {
        ResultPaginationDTO resultPaginationDTO= this.userService.getAllUsers(specification, pageable);
        return ResponseData.success(resultPaginationDTO, SuccessCode.GET_SUCCESS);
    }

    @GetMapping("/users/{id}")
    private ResponseData<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO userResponseDTO = this.userService.getUserById(id);
        return ResponseData.success(userResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @PutMapping("/users/{id}")
    public ResponseData<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO user) {
        this.userService.updateUser(id, user);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @DeleteMapping("/users/{id}")
    public ResponseData<?> deleteUser(@PathVariable Long id) {
        this.userService.deleteUser(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }

}
