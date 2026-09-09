package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.RegisterDTO;
import com.caochung.recruitment.dto.request.ResetPasswordRequestDTO;
import com.caochung.recruitment.dto.request.VerifyOtpDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.event.ForgotPasswordEvent;
import com.caochung.recruitment.event.UserRegisterEvent;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.messaging.dto.EmailNotificationMessage;
import com.caochung.recruitment.messaging.dto.NotificationType;
import com.caochung.recruitment.messaging.publisher.NotificationPublisher;
import com.caochung.recruitment.repository.RoleRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.AuthService;
import com.caochung.recruitment.service.RedisService;
import com.caochung.recruitment.service.mapper.UserMapper;
import com.caochung.recruitment.util.OtpGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic="AUTH-SERVICE")
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final ApplicationEventPublisher publisher;
    private final NotificationPublisher notificationPublisher;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper  userMapper;
    private final RoleRepository roleRepository;
    private final ObjectMapper mapper;

    /**
     * register account candidate to receive otp verification
     * @param registerDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserResponseDTO registerAndSendOtp(RegisterDTO registerDTO) {
        User existingUser = userRepository.findByEmailIgnoringSoftDelete(registerDTO.getEmail());
        if(existingUser != null){
            if(existingUser.getStatus().equals(UserStatusEnum.ACTIVE)){
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }
            if (existingUser.getStatus().equals(UserStatusEnum.DISABLED)) {
                throw new AppException(ErrorCode.USER_DISABLED);
            }
        }
        // Avoid spam otpCode
        if(redisService.isSpamming(registerDTO.getEmail())){
            throw new AppException(ErrorCode.WAITING_60_SECONDS_TO_SEND_NEW_VERIFICATION_CODE);
        }

        String otp = OtpGenerator.generateOtp();
        try{
            String userJson = mapper.writeValueAsString(registerDTO);
            redisService.saveRegisterData(registerDTO.getEmail(), otp, userJson);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Event send otp to user by Spring Event
        UserRegisterEvent event = UserRegisterEvent.builder()
                .email(registerDTO.getEmail())
                .otpToken(otp)
                .build();
        publisher.publishEvent(event);
        // RabbitMQ
        EmailNotificationMessage message = EmailNotificationMessage.builder()
                .emailTo(registerDTO.getEmail())
                .otpToken(otp)
                .notificationType(NotificationType.USER_REGISTER)
                .build();
        notificationPublisher.publish(message);
        return userMapper.toDto(userMapper.toEntity(registerDTO));
    }

    /**
     * Verification account
     * @param verifyOtpDTO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void VerifyOtp(VerifyOtpDTO verifyOtpDTO){
        String userJson = redisService.getRegisterData(verifyOtpDTO.getEmail());
        if(userJson == null){
            throw new AppException(ErrorCode.VERIFICATION_TIMEOUT);
        }
        String otp = redisService.getRegisterOtp(verifyOtpDTO.getEmail());
        if(otp == null || !otp.equals(verifyOtpDTO.getOtp())){
            throw new AppException(ErrorCode.VERIFICATION_INCORRECT);
        }
        try{
            RegisterDTO dto = mapper.readValue(userJson, RegisterDTO.class);
            User user = userMapper.toEntity(dto);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            Role roleDefault = Optional.ofNullable(roleRepository.findByName("CANDIDATE")).orElseThrow(
                    ()->new AppException(ErrorCode.ROLE_NOT_FOUND));
            user.setRole(roleDefault);
            user.setStatus(UserStatusEnum.ACTIVE);
            userRepository.save(user);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        redisService.clearRegistrationData(verifyOtpDTO.getEmail());
    }

    /**
     * send otp to forgot password email
     * @param email
     */
    @Override
    public void forgotPassword(String email) {
        User user = this.userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            String otp = OtpGenerator.generateOtp();

            this.redisService.savePasswordOtp(user.getEmail(), otp);
            //Spring Event
            ForgotPasswordEvent event = ForgotPasswordEvent.builder()
                    .email(user.getEmail())
                    .otpToken(otp)
                    .build();
            publisher.publishEvent(event);
            // RabbitMQ
            EmailNotificationMessage message = EmailNotificationMessage.builder()
                    .emailTo(user.getEmail())
                    .otpToken(otp)
                    .notificationType(NotificationType.FORGOT_PASSWORD)
                    .build();
            notificationPublisher.publish(message);
        }
    }

    /**
     * reset password with otp
     * @param resetPasswordRequestDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO) {
        String otpRedis = redisService.getPasswordOtp(resetPasswordRequestDTO.getEmail());
        if(otpRedis == null || !otpRedis.equals(resetPasswordRequestDTO.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OTP_CODE);
        }
        User user = this.userRepository.findByEmail(resetPasswordRequestDTO.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPassword(this.passwordEncoder.encode(resetPasswordRequestDTO.getNewPassword()));
        user.setRefreshToken(null);
        userRepository.save(user);
        redisService.deletePasswordOtp(resetPasswordRequestDTO.getEmail());
    }
}
