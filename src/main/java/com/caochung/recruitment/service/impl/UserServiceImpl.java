package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.RegisterDTO;
import com.caochung.recruitment.dto.request.UserRequestDTO;
import com.caochung.recruitment.dto.request.UserUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.ResumeRepository;
import com.caochung.recruitment.repository.RoleRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.UserService;
import com.caochung.recruitment.service.mapper.UserMapper;
import com.caochung.recruitment.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final ResumeRepository resumeRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if(userRepository.existsByEmail(userRequestDTO.getEmail())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        userRequestDTO.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        User user = userMapper.toEntity(userRequestDTO);
        user.setStatus(UserStatusEnum.ACTIVE);
        User saveUser = userRepository.save(user);
        return userMapper.toDto(saveUser);
    }

    @Override
    @Transactional
    public UserResponseDTO register(RegisterDTO registerDTO) {
        if(userRepository.existsByEmail(registerDTO.getEmail())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        registerDTO.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        User user = userMapper.toEntity(registerDTO);
        Role defaultRole = Optional.ofNullable(this.roleRepository.findByName("CANDIDATE")).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.setRole(defaultRole);
        user.setStatus(UserStatusEnum.ACTIVE);

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
    @Transactional
    public void updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.fromUpdate(userUpdateDTO, user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        String currentUser = SecurityUtil.getCurrentUserLogin().orElse("SYSTEM");
        resumeRepository.withdrawnResumeByUserId( Instant.now(),currentUser,user.getId());
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
    @Transactional
    public void updateUserToken(String token, String email) {
        User user = this.userRepository.findByEmail(email);
        if(user != null){
            user.setRefreshToken(token);
        }
    }

    @Override
    public User getUserByRefreshTokenAndEmail(String refreshToken,  String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }

    @Override
    public PaginationResponseDTO getAllDisableUser(Pageable pageable){
        Page<User> pageUsers = userRepository.findAllDisableUsers(pageable);
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
    @Transactional
    public void restoreUserById(Long id){
        User user = userRepository.findByIdIncludingDeleted(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));

        if(user.getStatus().equals(UserStatusEnum.ACTIVE)){
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE);
        }
        userRepository.restoreUserById(user.getId());
    }
}