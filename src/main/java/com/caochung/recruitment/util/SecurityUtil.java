package com.caochung.recruitment.util;

import com.caochung.recruitment.dto.response.LoginResponseDTO;
import com.caochung.recruitment.dto.response.RoleResponseDTO;
import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SecurityUtil {

    private final JwtEncoder jwtEncoder;

    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    @Value("${caochung.jwt.base64-secret}")
    private String jwtKey;

    @Value("${caochung.jwt.access-token-validity-in-second}")
    private long accessTokenExpiration;

    @Value("${caochung.jwt.refresh-token-validity-in-second}")
    private long refreshTokenExpiration;

    public String createAccessToken(String email, LoginResponseDTO loginResponseDTO) {
        LoginResponseDTO.UserInfo userInfo = loginResponseDTO.getUserInfo();

        List<String> authorities = new ArrayList<>();
        if(userInfo!=null && userInfo.getRole()!=null && userInfo.getRole().getPermissions()!=null){
            authorities = userInfo.getRole().getPermissions().stream()
                    .map(RoleResponseDTO.PermissionRole::getName).toList();
        }

        LoginResponseDTO.UserInsideToken userToken = LoginResponseDTO.UserInsideToken.builder()
                .id(loginResponseDTO.getUserInfo().getId())
                .email(loginResponseDTO.getUserInfo().getEmail())
                .username(loginResponseDTO.getUserInfo().getUsername())
                .build();

        Instant now = Instant.now();
        Instant expiration = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(email)
                .claim("user", userToken)
                .claim("permission", authorities)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String createRefreshToken(String email, LoginResponseDTO dto) {
        LoginResponseDTO.UserInsideToken userToken = LoginResponseDTO.UserInsideToken.builder()
                .id(dto.getUserInfo().getId())
                .email(dto.getUserInfo().getEmail())
                .username(dto.getUserInfo().getUsername())
                .build();

        Instant now = Instant.now();
        Instant expiration = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(email)
                .claim("user", userToken)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }

    public Jwt checkValidRefreshToken(String refreshToken) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JWT_ALGORITHM).build();
        try {
            return jwtDecoder.decode(refreshToken);
        } catch (Exception e) {
            System.out.println(">>> Refresh token error: " + e.getMessage());
            throw e;
        }
    }

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(authentication -> authentication.getCredentials() instanceof String)
                .map(authentication -> (String) authentication.getCredentials());
    }
}
