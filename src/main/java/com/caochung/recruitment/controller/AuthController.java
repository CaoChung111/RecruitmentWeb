package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.LoginDTO;
import com.caochung.recruitment.dto.request.RegisterDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.LoginResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.service.UserService;
import com.caochung.recruitment.service.mapper.RoleMapper;
import com.caochung.recruitment.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Authentication & Authorization", description = "APIs for user authentication, registration, and token management.")
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userServiceImpl;
    private final RoleMapper roleMapper;

    @Value("${caochung.jwt.refresh-token-validity-in-second}")
    private long refreshTokenExpiration;

    @Operation(summary = "User Login", description = "Authenticates a user with username and password, returning access and refresh tokens. Sets refresh token as an HTTP-only cookie.")
    @PostMapping("/auth/login")
    public ResponseEntity<ResponseData<LoginResponseDTO>> Login(@Valid @RequestBody LoginDTO loginDTO) {
        //Nạp input vào Security
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
        //Xác thực người dùng
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(token);

        //Tạo token
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = this.userServiceImpl.getUserByUsername(loginDTO.getUsername());

        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .userInfo(LoginResponseDTO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getName())
                        .email(user.getEmail())
                        .role(roleMapper.toDTO(user.getRole()))
                        .build())
                .build();
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), loginResponseDTO);
        loginResponseDTO.setAccessToken(accessToken);
        String refreshToken = this.securityUtil.createRefreshToken(user.getEmail(), loginResponseDTO);
        this.userServiceImpl.updateUserToken(refreshToken, user.getEmail());

        ResponseCookie responseCookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite("None")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(ResponseData.success(loginResponseDTO, SuccessCode.LOGIN_SUCCESS));
    }

    @Operation(summary = "User Logout", description = "Logs out the current user by invalidating their refresh token and clearing the refresh token cookie. Requires authentication.")
    @PostMapping("/auth/logout")
    public ResponseEntity<ResponseData<?>> Logout() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        if(email.isEmpty()){
            throw new AppException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        this.userServiceImpl.updateUserToken(null, email);

        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refreshToken", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .body(ResponseData.success(SuccessCode.LOGOUT_SUCCESS));
    }

    @Operation(summary = "Get Current User Account", description = "Retrieves the details of the currently authenticated user. Requires authentication.")
    @GetMapping("/auth/account")
    public ResponseEntity<ResponseData<LoginResponseDTO>> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        User user = this.userServiceImpl.getUserByUsername(email);
        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .userInfo(LoginResponseDTO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getName())
                        .email(user.getEmail())
                        .role(roleMapper.toDTO(user.getRole()))
                        .build())
                .build();
        return ResponseEntity.ok().body(ResponseData.success(loginResponseDTO, SuccessCode.GET_SUCCESS));
    }

    @Operation(summary = "Refresh Access Token", description = "Generates a new access token using a valid refresh token provided in an HTTP-only cookie. Also issues a new refresh token. Requires a valid refresh token.")
    @GetMapping("/auth/refresh")
    public ResponseEntity<ResponseData<LoginResponseDTO>> getRefreshToken(
            @CookieValue(name = "refreshToken", defaultValue = "defaultRefreshToken") String refreshToken) {
        if(refreshToken.equals("defaultRefreshToken")) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        Jwt decodeRefreshToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodeRefreshToken.getSubject();

        User user = this.userServiceImpl.getUserByRefreshTokenAndEmail(refreshToken, email);
        if (user == null) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .userInfo(LoginResponseDTO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getName())
                        .email(user.getEmail())
                        .role(roleMapper.toDTO(user.getRole()))
                        .build())
                .build();
        String accessToken = this.securityUtil.createAccessToken(email, loginResponseDTO);
        loginResponseDTO.setAccessToken(accessToken);
        String newRefreshToken = this.securityUtil.createRefreshToken(email, loginResponseDTO);
        this.userServiceImpl.updateUserToken(refreshToken, email);

        ResponseCookie responseCookie = ResponseCookie
                .from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite("None")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(ResponseData.success(loginResponseDTO, SuccessCode.LOGIN_SUCCESS));
    }

    @Operation(summary = "Register New User", description = "Registers a new user account with the provided details. No authentication required.")
    @PostMapping("/auth/register")
    public ResponseData<UserResponseDTO> registerUser(@Valid @RequestBody RegisterDTO registerDTO) {
        UserResponseDTO userResponseDTO = this.userServiceImpl.register(registerDTO);
        return ResponseData.success(userResponseDTO
                , SuccessCode.CREATED_SUCCESS);
    }
}
