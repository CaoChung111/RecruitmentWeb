package com.caochung.recruitment.service;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.UserRequestDTO;
import com.caochung.recruitment.dto.request.UserUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserMapper userMapper;

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if(userRepository.existsByEmail(userRequestDTO.getEmail())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        companyRepository.findById(userRequestDTO.getCompany().getId()).orElseThrow(
                () -> new AppException(ErrorCode.COMPANY_NOT_FOUND)
        );
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userRequestDTO.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        User user = userMapper.toEntity(userRequestDTO);
        User saveUser = userRepository.save(user);
        UserResponseDTO userResponseDTO = userMapper.toDto(saveUser);
        return userResponseDTO;
    }

    public PaginationResponseDTO getAllUsers(Specification<User> specification, Pageable pageable) {
        Page<User> pageUsers = userRepository.findAll(specification, pageable);
        List<UserResponseDTO> userResponseDTOs = userMapper.toDto(pageUsers.getContent());

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(pageUsers.getTotalPages())
                .totalItems(pageUsers.getTotalElements())
                .build();

        return new PaginationResponseDTO(meta, userResponseDTOs);
    }

    public void updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        companyRepository.findById(userUpdateDTO.getCompany().getId()).orElseThrow(
                () -> new AppException(ErrorCode.COMPANY_NOT_FOUND)
        );
        userMapper.fromUpdate(userUpdateDTO, user);
        this.userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    public User getUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserResponseDTO userResponseDTO = userMapper.toDto(user);
        return userResponseDTO;
    }

    public void updateUserToken(String token, String email) {
        User user = this.getUserByUsername(email);
        if(user != null){
            user.setRefreshToken(token);
            this.userRepository.save(user);
        }
    }

    public User getUserByRefreshTokenAndEmail(String refreshToken,  String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }


}
