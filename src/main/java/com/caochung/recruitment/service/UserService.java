package com.caochung.recruitment.service;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.UserRequestDTO;
import com.caochung.recruitment.dto.request.UserUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.exception.AppException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO userRequestDTO);

    PaginationResponseDTO getAllUsers(Specification<User> specification, Pageable pageable);

    void updateUser(Long id, UserUpdateDTO userUpdateDTO);

    void deleteUser(Long id);

    User getUserByUsername(String username);

    UserResponseDTO getUserById(Long id);

    void updateUserToken(String token, String email);

    User getUserByRefreshTokenAndEmail(String refreshToken,  String email);
}
