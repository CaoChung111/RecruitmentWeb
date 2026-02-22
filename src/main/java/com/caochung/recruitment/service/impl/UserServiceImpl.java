package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.RegisterDTO;
import com.caochung.recruitment.dto.request.UserRequestDTO;
import com.caochung.recruitment.dto.request.UserUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.RoleRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.UserService;
import com.caochung.recruitment.service.mapper.UserMapper;
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
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;


    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if(userRepository.existsByEmail(userRequestDTO.getEmail())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userRequestDTO.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        User user = userMapper.toEntity(userRequestDTO);
        User saveUser = userRepository.save(user);
        return userMapper.toDto(saveUser);
    }

    @Override
    public UserResponseDTO register(RegisterDTO registerDTO) {
        if(userRepository.existsByEmail(registerDTO.getEmail())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        registerDTO.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        User user = userMapper.toEntity(registerDTO);
        Role defaultRole = this.roleRepository.findByName("CANDIDATE");
        user.setRole(defaultRole);

        User saveUser = userRepository.save(user);
        return userMapper.toDto(saveUser);
    }

    @Override
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

    @Override
    public void updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.fromUpdate(userUpdateDTO, user);
        this.userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    @Override
    public User getUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toDto(user);
    }

    @Override
    public void updateUserToken(String token, String email) {
        User user = this.getUserByUsername(email);
        if(user != null){
            user.setRefreshToken(token);
            this.userRepository.save(user);
        }
    }

    @Override
    public User getUserByRefreshTokenAndEmail(String refreshToken,  String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }


}
