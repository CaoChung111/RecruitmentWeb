package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.domain.dto.request.LoginDTO;
import com.caochung.recruitment.domain.dto.response.ResponseData;
import com.caochung.recruitment.domain.dto.response.ResponseLoginDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.service.UserService;
import com.caochung.recruitment.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    @Value("${caochung.jwt.refresh-token-validity-in-second}")
    private long refreshTokenExpiration;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil, UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResponseData<ResponseLoginDTO>> Login(@Valid @RequestBody LoginDTO loginDTO) {
        //Nạp input vào Security
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
        //Xác thực người dùng
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(token);

        //Tạo token
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = this.userService.getUserByUsername(loginDTO.getUsername());

        ResponseLoginDTO responseLoginDTO = ResponseLoginDTO.builder()
                .userInfo(ResponseLoginDTO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getName())
                        .email(user.getEmail())
                        .build())
                .build();
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), responseLoginDTO.getUserInfo());
        responseLoginDTO.setAccessToken(accessToken);
        String refreshToken = this.securityUtil.createRefreshToken(user.getEmail(), responseLoginDTO);
        this.userService.updateUserToken(refreshToken, user.getEmail());

        ResponseCookie responseCookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(ResponseData.success(responseLoginDTO, SuccessCode.LOGIN_SUCCESS));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ResponseData<?>> Logout() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        if(email.isEmpty()){
            throw new AppException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        this.userService.updateUserToken(null, email);

        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refreshToken", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .body(ResponseData.success(SuccessCode.LOGOUT_SUCCESS));
    }

    @GetMapping("/auth/account")
    public ResponseEntity<ResponseData<ResponseLoginDTO>> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        User user = this.userService.getUserByUsername(email);
        ResponseLoginDTO responseLoginDTO = ResponseLoginDTO.builder()
                .userInfo(ResponseLoginDTO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getName())
                        .email(user.getEmail())
                        .build())
                .build();
        return ResponseEntity.ok().body(ResponseData.success(responseLoginDTO, SuccessCode.GET_SUCCESS));
    }

    @GetMapping("/auth/refresh")
    public ResponseEntity<ResponseData<ResponseLoginDTO>> getRefreshToken(
            @CookieValue(name = "refreshToken", defaultValue = "defaultRefreshToken") String refreshToken) {
        if(refreshToken.equals("defaultRefreshToken")) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        Jwt decodeRefreshToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodeRefreshToken.getSubject();

        User user = this.userService.getUserByRefreshTokenAndEmail(refreshToken, email);
        if (user == null) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        ResponseLoginDTO responseLoginDTO = ResponseLoginDTO.builder()
                .userInfo(ResponseLoginDTO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getName())
                        .email(user.getEmail())
                        .build())
                .build();
        String accessToken = this.securityUtil.createAccessToken(email, responseLoginDTO.getUserInfo());
        responseLoginDTO.setAccessToken(accessToken);
        String newRefreshToken = this.securityUtil.createRefreshToken(email, responseLoginDTO);
        this.userService.updateUserToken(refreshToken, email);

        ResponseCookie responseCookie = ResponseCookie
                .from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(ResponseData.success(responseLoginDTO, SuccessCode.LOGIN_SUCCESS));
    }
}
