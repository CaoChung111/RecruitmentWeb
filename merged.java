// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\RecruitmentApplication.java =====
package com.caochung.recruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
public class RecruitmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecruitmentApplication.class, args);
	}

}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\CloudinaryConfig.java =====
package com.caochung.recruitment.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\CorsConfig.java =====
package com.caochung.recruitment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4173", "http://localhost:5173/", "https://recruitment-web-fe.vercel.app", "http://localhost:8080")); // Thêm localhost:8080 để đảm bảo truy cập Swagger UI từ chính backend
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "x_no_retry"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\CustomAuthenticationEntryPoint.java =====
package com.caochung.recruitment.config;

import com.caochung.recruitment.dto.response.ResponseError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper mapper;
    public CustomAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        this.delegate.commence(request, response, authException);
        response.setContentType("application/json;charset=UTF-8");

        ResponseError responseError = new ResponseError();
        responseError.setTimestamp(new Date());
        responseError.setStatus(HttpStatus.UNAUTHORIZED.value());
        responseError.setPath(request.getRequestURI());
        String errorMessage = Optional.ofNullable(authException.getCause()).map(Throwable::getMessage).orElse(authException.getMessage());
        responseError.setError(errorMessage);
        responseError.setMessage("Token không hợp lệ (hết hạn, không đúng định dạng)");
        mapper.writeValue(response.getWriter(), responseError);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\CustomSecurityExpression.java =====
package com.caochung.recruitment.config;

import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.ResumeRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("customSecurity")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomSecurityExpression {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final CompanyRepository companyRepository;

    // Lấy User hợp lệ (Tạm giữ nguyên, nhưng đã gom logic check cho gọn)
    private User getValidCurrentUser() {
        return SecurityUtil.getCurrentUserLogin()
                .flatMap(userRepository::findByEmail)
                .filter(user -> UserStatusEnum.ACTIVE.equals(user.getStatus()))
                .orElse(null);
    }

    private boolean isSuperAdmin(User user) {
        return user.getRole().getName().equals("SUPER_ADMIN");
    }

    // 1. Check quyền User
    public boolean isUserOwner(Long userId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;

        return isSuperAdmin(currentUser) || currentUser.getId().equals(userId);
    }

    // 2. Check quyền sở hữu Job (HR check Job của công ty mình)
    public boolean isJobOwner(Long jobId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (isSuperAdmin(currentUser)) return true;

        if (currentUser.getCompany() == null) return false;

        // Tối ưu: Dùng exists (Chỉ SELECT 1 record) thay vì load cả entity Job
        return jobRepository.existsByIdAndCompany_Id(jobId, currentUser.getCompany().getId());
    }

    // 3. Check quyền sở hữu Resume (Ứng viên thao tác với CV của mình)
    public boolean isResumeOwner(Long resumeId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (isSuperAdmin(currentUser)) return true;

        // Tối ưu: Dùng exists
        return resumeRepository.existsByIdAndUser_Id(resumeId, currentUser.getId());
    }

    // 4. Check quyền HR với Resume (HR chỉ được xem CV nộp vào công ty mình)
    public boolean isResumeInRecruiterCompany(Long resumeId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (isSuperAdmin(currentUser)) return true;
        if (currentUser.getCompany() == null) return false;

        // Tối ưu đỉnh cao: Xuyên qua 2 bảng (Resume -> Job -> Company) chỉ bằng 1 hàm exists
        return resumeRepository.existsByIdAndJob_Company_Id(resumeId, currentUser.getCompany().getId());
    }

    // 5. Check quyền sở hữu Company
    public boolean isCompanyOwner(Long companyId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (isSuperAdmin(currentUser)) return true;

        return currentUser.getCompany() != null && currentUser.getCompany().getId().equals(companyId);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\CustomUserDetails.java =====
package com.caochung.recruitment.config;

import com.caochung.recruitment.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final Role role;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\CustomUserDetailsService.java =====
package com.caochung.recruitment.config;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("userDetailService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        if (user.getStatus().equals(UserStatusEnum.DISABLED)) {
            throw new AppException(ErrorCode.USER_DISABLED);
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if(user.getRole() != null && user.getRole().getPermissions() != null) {
            authorities = user.getRole().getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getName())).toList();
        }

        return new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(), user.getName(), user.getRole(), authorities);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\DatabaseInitializer.java =====
package com.caochung.recruitment.config;

import com.caochung.recruitment.constant.PermissionEnum;
import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.repository.PermissionRepository;
import com.caochung.recruitment.repository.RoleRepository;
import com.caochung.recruitment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("RUN FAKE DATABASE INITIALIZER");
        List<Permission> allPermissions = seedPermissions();
        seedRoles(allPermissions);
        seedUsers();
        System.out.println("RUN FAKE DATABASE INITIALIZER");
    }

    private List<Permission> seedPermissions() {
        if (this.permissionRepository.count() > 0) {
            return this.permissionRepository.findAll();
        }

        ArrayList<Permission> arr = new ArrayList<>();

        for (PermissionEnum p : PermissionEnum.values()) {
            Permission permission = new Permission();
            permission.setName(p.getName());
            permission.setApiPath(p.getApiPath());
            permission.setMethod(p.getMethod());
            permission.setModule(p.getModule());

            arr.add(permission);
        }
        return this.permissionRepository.saveAll(arr);
    }

    private void seedRoles(List<Permission> allPermissions) {
        // --- 1. SUPER_ADMIN ---
        if (this.roleRepository.findByName("SUPER_ADMIN") == null) {
            Role admin = new Role();
            admin.setName("SUPER_ADMIN");
            admin.setDescription("Quản trị viên hệ thống - Full Quyền");
            admin.setActive(true);
            admin.setPermissions(new HashSet<>(allPermissions));
            this.roleRepository.save(admin);
            System.out.println(">>> Initialized ROLE: SUPER_ADMIN");
        }

        // --- 2. CANDIDATE ---
        if (this.roleRepository.findByName("CANDIDATE") == null) {
            Role candidate = new Role();
            candidate.setName("CANDIDATE");
            candidate.setDescription("Ứng viên tìm việc");
            candidate.setActive(true);

            List<String> candidatePermissions = List.of(
                    // Resumes: Quản lý hồ sơ CÁ NHÂN
                    "RESUME_CREATE",
                    "RESUME_DELETE",
                    "RESUME_VIEW_OWN",
                    "RESUME_VIEW_DETAIL",

                    // Users: Quản lý tài khoản
                    "USER_UPDATE",
                    "USER_VIEW_DETAIL",

                    // Files: Upload CV
                    "FILE_UPLOAD",

                    // Subscribers: Nhận tin tuyển dụng
                    "SUBSCRIBER_CREATE"
            );

            List<Permission> permissions = allPermissions.stream()
                    .filter(p -> candidatePermissions.contains(p.getName()))
                    .toList();

            candidate.setPermissions(new HashSet<>(permissions));
            this.roleRepository.save(candidate);
            System.out.println(">>> Initialized ROLE: CANDIDATE");
        }

        // --- 3. RECRUITER ---
        if (this.roleRepository.findByName("RECRUITER") == null) {
            Role recruiter = new Role();
            recruiter.setName("RECRUITER");
            recruiter.setDescription("Nhà tuyển dụng - Đăng tin & Xem hồ sơ");
            recruiter.setActive(true);
            List<String> recruiterPermissions = List.of(
                    // Jobs: Quản lý tin tuyển dụng
                    "JOB_CREATE",
                    "JOB_UPDATE",
                    "JOB_DELETE",

                    // Companies: Quản lý thông tin công ty
                    "COMPANY_UPDATE",

                    // Resumes: Xem ứng viên
                    "RESUME_VIEW_COMPANY",
                    "RESUME_VIEW_DETAIL",
                    "RESUME_UPDATE",

                    // Users: Quản lý tài khoản
                    "USER_UPDATE",
                    "USER_VIEW_DETAIL",

                    // Files: Upload Logo cty
                    "FILE_UPLOAD"
            );

            List<Permission> permissions = allPermissions.stream()
                    .filter(p -> recruiterPermissions.contains(p.getName()))
                    .toList();
            recruiter.setPermissions(new HashSet<>(allPermissions));
            this.roleRepository.save(recruiter);
            System.out.println(">>> Initialized ROLE: RECRUITER");
        }
    }

    private void seedUsers() {
        Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");

        if (this.userRepository.count() == 0 && adminRole != null) {
            User user = new User();
            user.setEmail("admin@gmail.com");
            user.setName("Super Admin");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole(adminRole);

            this.userRepository.save(user);
            System.out.println(">>> Initialized USER: admin@gmail.com");
        }
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\DateTimeFormatConfiguration.java =====
package com.caochung.recruitment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Formattable;

@Configuration
public class DateTimeFormatConfiguration implements WebMvcConfigurer{

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\JpaConfiguration.java =====
package com.caochung.recruitment.config;

import com.caochung.recruitment.util.SecurityUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfiguration {
    @Bean
    public AuditorAware<String> auditorProvider(){
        return SecurityUtil::getCurrentUserLogin;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\OpenApiConfig.java =====
package com.caochung.recruitment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("dev")
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(@Value("${open.api.title}") String title,
                           @Value("${open.api.version}") String version,
                           @Value("${open.api.description}") String description,
                           @Value("${open.api.serverUrl}") String serverUrl,
                           @Value("${open.api.serverName}") String serverName) {
        return new OpenAPI().info(new Info()
                        .title(title)
                        .version(version)
                        .description(description)
                        .license(new License().name("API License").url("http://domain.vn/license")))
                .servers(List.of(new Server().url(serverUrl).description(serverName)))
                .components(
                        new Components()
                                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .security(List.of(new SecurityRequirement().addList("bearerAuth")));
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("api-recruitment-service")
                .packagesToScan("com.caochung.recruitment.controller")
                .build();
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\RedisConfig.java =====
package com.caochung.recruitment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);

        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper redisObjectMapper) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper())));
        // Cấu hình TTL riêng cho từng cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("skill_detail", redisCacheConfiguration.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("job_detail", redisCacheConfiguration.entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put("company_detail", redisCacheConfiguration.entryTtl(Duration.ofDays(1)));
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\config\SecurityConfiguration.java =====
package com.caochung.recruitment.config;

import com.caochung.recruitment.service.RedisService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static com.caochung.recruitment.util.SecurityUtil.JWT_ALGORITHM;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@Slf4j
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final RedisService redisService;
    @Value("${caochung.jwt.base64-secret}")
    private String jwtKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager  authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        String[] whiteList = {
                "/",
                "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/register", "/api/v1/auth/verify-otp",
                "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password",
                "/storage/**",
                "/swagger-ui/**", "/v3/api-docs/**",
        };
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(
                        authz -> authz
                                .requestMatchers(whiteList).permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/companies", "/api/v1/companies/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/jobs", "/api/v1/jobs/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/skills", "/api/v1/skills/**").permitAll()
                                .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))
//                .exceptionHandling( exceptions -> exceptions
//                        .authenticationEntryPoint(customAuthenticationEntryPoint)
//                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter  jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("permission");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JWT_ALGORITHM).build();
        return token -> {
            if(redisService.isExpireToken(token)) {
                log.info("Token expired");
                throw new JwtException("Token has been revoked");
            }
            try {
                return jwtDecoder.decode(token);
            } catch (Exception e) {
                log.warn(">>> JWT Validation error: {}", e.getMessage());
                throw e;
            }
        };
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\constant\CompanyStatusEnum.java =====
package com.caochung.recruitment.constant;

public enum CompanyStatusEnum {
    ACTIVE,
    INACTIVE,
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\constant\ErrorCode.java =====
package com.caochung.recruitment.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION("Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY("Invalid message key", HttpStatus.BAD_REQUEST),

    INVALID_ACCESS_TOKEN("Invalid access token", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("Invalid refresh token", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED("Unauthenticated", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("Access denied", HttpStatus.FORBIDDEN),

    VALIDATION_ERROR("Validation failed", HttpStatus.BAD_REQUEST),
    VERIFICATION_TIMEOUT("Verification timed out", HttpStatus.UNAUTHORIZED),
    VERIFICATION_INCORRECT("Verification incorrect", HttpStatus.BAD_REQUEST),
    WAITING_60_SECONDS_TO_SEND_NEW_VERIFICATION_CODE("Waiting 60 Seconds to send new verification code", HttpStatus.BAD_REQUEST),

    INVALID_OTP_CODE("Invalid OTP code", HttpStatus.BAD_REQUEST),

    USER_EXISTED("User already existed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),
    EMAIL_EXISTED("Email already existed", HttpStatus.BAD_REQUEST),
    USER_DISABLED("User disabled", HttpStatus.UNAUTHORIZED),
    USER_ALREADY_ACTIVE("User already active", HttpStatus.BAD_REQUEST),

    FILE_ERROR("File error", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_TOO_LARGE("File too long", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_EMPTY("File is empty", HttpStatus.BAD_REQUEST),

    COMPANY_EXISTED("Company already existed", HttpStatus.BAD_REQUEST),
    COMPANY_NOT_FOUND("Company not found", HttpStatus.NOT_FOUND),
    COMPANY_INACTIVE("Company inactive", HttpStatus.BAD_REQUEST),
    COMPANY_ALREADY_ACTIVE("Company already active", HttpStatus.BAD_REQUEST),

    SKILL_EXISTED("Skill already existed", HttpStatus.BAD_REQUEST),
    SKILL_NOT_FOUND("Skill not found", HttpStatus.NOT_FOUND),
    SKILL_HAS_USED("Skill has been used", HttpStatus.BAD_REQUEST),

    JOB_EXISTED("Job already existed", HttpStatus.BAD_REQUEST),
    JOB_NOT_FOUND("Job not found", HttpStatus.NOT_FOUND),
    JOB_HAS_ACTIVE_RESUMES("Job has resumes", HttpStatus.BAD_REQUEST),
    JOB_INACTIVE("Job has been inactive", HttpStatus.BAD_REQUEST),

    RESUME_NOT_FOUND("Resume not found",  HttpStatus.NOT_FOUND),
    ALREADY_APPLIED("Already applied", HttpStatus.BAD_REQUEST),

    PERMISSION_NOT_FOUND("Permission not found",  HttpStatus.NOT_FOUND),
    PERMISSION_EXISTED("Permission already existed",  HttpStatus.BAD_REQUEST),
    PERMISSION_HAS_USED("Permission has been used", HttpStatus.BAD_REQUEST),

    ROLE_EXISTED("Role already existed",  HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND("Role not found",  HttpStatus.NOT_FOUND),
    ROLE_ALREADY_ACTIVE("Role has been active",  HttpStatus.BAD_REQUEST),

    SUBSCRIBER_EXISTED("Subscriber already existed",  HttpStatus.BAD_REQUEST),
    SUBSCRIBER_NOT_FOUND("Subscriber not found",  HttpStatus.NOT_FOUND),;


    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\constant\GenderEnum.java =====
package com.caochung.recruitment.constant;

import lombok.Getter;

@Getter
public enum GenderEnum {
    MALE,
    FEMALE,
    OTHER;

    GenderEnum() {}
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\constant\JobStatusEnum.java =====
package com.caochung.recruitment.constant;

public enum JobStatusEnum {
    OPEN,
    CLOSED,
    DRAFT,
    FILLED,
    INACTIVE;

    JobStatusEnum() {}
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\constant\LevelEnum.java =====
package com.caochung.recruitment.constant;

public enum LevelEnum {
    INTERN,
    FRESHER,
    JUNIOR,
    MIDDLE,
    SENIOR,
    LEAD;
    LevelEnum() {}
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\constant\PermissionEnum.java =====
package com.caochung.recruitment.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionEnum {
    // 1. COMPANIES
    COMPANY_CREATE("COMPANY_CREATE", "/api/v1/companies", "POST", "COMPANIES"),
    COMPANY_UPDATE("COMPANY_UPDATE", "/api/v1/companies", "PUT", "COMPANIES"),
    COMPANY_DELETE("COMPANY_DELETE", "/api/v1/companies/{id}", "DELETE", "COMPANIES"),
    COMPANY_VIEW_DETAIL("COMPANY_VIEW_DETAIL", "/api/v1/companies/{id}", "GET", "COMPANIES"),
    COMPANY_VIEW_ALL("COMPANY_VIEW_ALL", "/api/v1/companies", "GET", "COMPANIES"),
    COMPANY_VIEW_INACTIVE("COMPANY_VIEW_INACTIVE", "/api/v1/companies/trash", "GET", "COMPANIES"),
    COMPANY_RESTORE("COMPANY_RESTORE", "/api/v1/companies/{id}/restore", "PUT", "COMPANIES"),

    // 2. JOBS
    JOB_CREATE("JOB_CREATE", "/api/v1/jobs", "POST", "JOBS"),
    JOB_UPDATE("JOB_UPDATE", "/api/v1/jobs", "PUT", "JOBS"),
    JOB_DELETE("JOB_DELETE", "/api/v1/jobs/{id}", "DELETE", "JOBS"),
    JOB_VIEW_DETAIL("JOB_VIEW_DETAIL", "/api/v1/jobs/{id}", "GET", "JOBS"),
    JOB_VIEW_ALL("JOB_VIEW_ALL", "/api/v1/jobs", "GET", "JOBS"),

    // 3. SKILLS
    SKILL_CREATE("SKILL_CREATE", "/api/v1/skills", "POST", "SKILLS"),
    SKILL_UPDATE("SKILL_UPDATE", "/api/v1/skills", "PUT", "SKILLS"),
    SKILL_DELETE("SKILL_DELETE", "/api/v1/skills/{id}", "DELETE", "SKILLS"),
    SKILL_VIEW_DETAIL("SKILL_VIEW_DETAIL", "/api/v1/skills/{id}", "GET", "SKILLS"),
    SKILL_VIEW_ALL("SKILL_VIEW_ALL", "/api/v1/skills", "GET", "SKILLS"),


    // 4. PERMISSIONS
    PERMISSION_CREATE("PERMISSION_CREATE", "/api/v1/permissions", "POST", "PERMISSIONS"),
    PERMISSION_UPDATE("PERMISSION_UPDATE", "/api/v1/permissions", "PUT", "PERMISSIONS"),
    PERMISSION_DELETE("PERMISSION_DELETE", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"),
    PERMISSION_VIEW_DETAIL("PERMISSION_VIEW_DETAIL", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"),
    PERMISSION_VIEW_ALL("PERMISSION_VIEW_ALL", "/api/v1/permissions", "GET", "PERMISSIONS"),

    // 5. RESUMES
    RESUME_CREATE("RESUME_CREATE", "/api/v1/resumes", "POST", "RESUMES"),
    RESUME_UPDATE("RESUME_UPDATE", "/api/v1/resumes", "PUT", "RESUMES"),
    RESUME_DELETE("RESUME_DELETE", "/api/v1/resumes/{id}", "DELETE", "RESUMES"),
    RESUME_VIEW_DETAIL("RESUME_VIEW_DETAIL", "/api/v1/resumes/{id}", "GET", "RESUMES"),
    RESUME_VIEW_ALL("RESUME_VIEW_ALL", "/api/v1/resumes", "GET", "RESUMES"),
    RESUME_VIEW_OWN("RESUME_VIEW_OWN", "/api/v1/resumes/by-user", "GET", "RESUMES"),
    RESUME_VIEW_COMPANY("RESUME_VIEW_COMPANY", "/api/v1/resumes/by-company", "GET", "RESUMES"),

    // 6. ROLES
    ROLE_CREATE("ROLE_CREATE", "/api/v1/roles", "POST", "ROLES"),
    ROLE_UPDATE("ROLE_UPDATE", "/api/v1/roles", "PUT", "ROLES"),
    ROLE_DELETE("ROLE_DELETE", "/api/v1/roles/{id}", "DELETE", "ROLES"),
    ROLE_VIEW_DETAIL("ROLE_VIEW_DETAIL", "/api/v1/roles/{id}", "GET", "ROLES"),
    ROLE_VIEW_ALL("ROLE_VIEW_ALL", "/api/v1/roles", "GET", "ROLES"),

    // 7. USERS
    USER_CREATE("USER_CREATE", "/api/v1/users", "POST", "USERS"),
    USER_UPDATE("USER_UPDATE", "/api/v1/users", "PUT", "USERS"),
    USER_DELETE("USER_DELETE", "/api/v1/users/{id}", "DELETE", "USERS"),
    USER_VIEW_DETAIL("USER_VIEW_DETAIL", "/api/v1/users/{id}", "GET", "USERS"),
    USER_VIEW_ALL("USER_VIEW_ALL", "/api/v1/users", "GET", "USERS"),
    USER_VIEW_DISABLE("USER_VIEW_DISABLE", "/api/v1/users/trash", "GET", "USERS"),
    USER_RESTORE("USER_RESTORE", "/api/v1/users/{id}/restore", "PUT", "USERS"),


    // 8. SUBSCRIBERS
    SUBSCRIBER_CREATE("SUBSCRIBER_CREATE", "/api/v1/subscribers", "POST", "SUBSCRIBERS"),
    SUBSCRIBER_UPDATE("SUBSCRIBER_UPDATE", "/api/v1/subscribers", "PUT", "SUBSCRIBERS"),
    SUBSCRIBER_DELETE("SUBSCRIBER_DELETE", "/api/v1/subscribers/{id}", "DELETE", "SUBSCRIBERS"),
    SUBSCRIBER_VIEW_DETAIL("SUBSCRIBER_VIEW_DETAIL", "/api/v1/subscribers/{id}", "GET", "SUBSCRIBERS"),
    SUBSCRIBER_VIEW_ALL("SUBSCRIBER_VIEW_ALL", "/api/v1/subscribers", "GET", "SUBSCRIBERS"),

    // 9. FILES
    FILE_UPLOAD("FILE_UPLOAD", "/api/v1/files/uploads", "POST", "FILES");

    private final String name;
    private final String apiPath;
    private final String method;
    private final String module;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\constant\ResumeStatusEnum.java =====
package com.caochung.recruitment.constant;

public enum ResumeStatusEnum {
    PENDING,
    REVIEWING,
    APPROVED,
    REJECTED,
    WITHDRAWN,
    SYSTEM_CANCEL;
    ResumeStatusEnum() {}
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\constant\SecurityConstant.java =====
package com.caochung.recruitment.constant;

public final class SecurityConstant {

    private SecurityConstant() {}

    // ================= 1. COMPANIES =================
    public static final String COMPANY_CREATE = "hasAuthority('COMPANY_CREATE')";
    public static final String COMPANY_UPDATE = "hasAuthority('COMPANY_UPDATE') and @customSecurity.isCompanyOwner(#id)";
    public static final String COMPANY_DELETE = "hasAuthority('COMPANY_DELETE')";
    public static final String COMPANY_VIEW_DETAIL = "hasAuthority('COMPANY_VIEW_DETAIL')";
    public static final String COMPANY_VIEW_ALL = "hasAuthority('COMPANY_VIEW_ALL')";
    public static final String COMPANY_VIEW_INACTIVE = "hasAuthority('COMPANY_VIEW_INACTIVE')";
    public static final String COMPANY_RESTORE = "hasAuthority('COMPANY_RESTORE')";

    // ================= 2. JOBS =================
    public static final String JOB_CREATE = "hasAuthority('JOB_CREATE')";
    public static final String JOB_UPDATE = "hasAuthority('JOB_UPDATE') and @customSecurity.isJobOwner(#id)";
    public static final String JOB_DELETE = "hasAuthority('JOB_DELETE') and @customSecurity.isJobOwner(#id)";
    public static final String JOB_VIEW_DETAIL = "hasAuthority('JOB_VIEW_DETAIL')";
    public static final String JOB_VIEW_ALL = "hasAuthority('JOB_VIEW_ALL')";

    // ================= 3. SKILLS =================
    public static final String SKILL_CREATE = "hasAuthority('SKILL_CREATE')";
    public static final String SKILL_UPDATE = "hasAuthority('SKILL_UPDATE')";
    public static final String SKILL_DELETE = "hasAuthority('SKILL_DELETE')";
    public static final String SKILL_VIEW_DETAIL = "hasAuthority('SKILL_VIEW_DETAIL')";
    public static final String SKILL_VIEW_ALL = "hasAuthority('SKILL_VIEW_ALL')";

    // ================= 4. PERMISSIONS =================
    public static final String PERMISSION_CREATE = "hasAuthority('PERMISSION_CREATE')";
    public static final String PERMISSION_UPDATE = "hasAuthority('PERMISSION_UPDATE')";
    public static final String PERMISSION_DELETE = "hasAuthority('PERMISSION_DELETE')";
    public static final String PERMISSION_VIEW_DETAIL = "hasAuthority('PERMISSION_VIEW_DETAIL')";
    public static final String PERMISSION_VIEW_ALL = "hasAuthority('PERMISSION_VIEW_ALL')";

    // ================= 5. RESUMES =================
    public static final String RESUME_CREATE = "hasAuthority('RESUME_CREATE')";
    public static final String RESUME_UPDATE = "hasAuthority('RESUME_UPDATE') and @customSecurity.isResumeInRecruiterCompany(#id)";
    public static final String RESUME_DELETE = "hasAuthority('RESUME_DELETE') and @customSecurity.isResumeOwner(#id)";
    public static final String RESUME_VIEW_DETAIL = "hasAuthority('RESUME_VIEW_DETAIL') and (@customSecurity.isResumeOwner(#id) or @customSecurity.isResumeInRecruiterCompany(#id))";
    public static final String RESUME_VIEW_ALL = "hasAuthority('RESUME_VIEW_ALL')";
    public static final String RESUME_VIEW_OWN = "hasAuthority('RESUME_VIEW_OWN')";
    public static final String RESUME_VIEW_COMPANY = "hasAuthority('RESUME_VIEW_COMPANY')";

    // ================= 6. ROLES =================
    public static final String ROLE_CREATE = "hasAuthority('ROLE_CREATE')";
    public static final String ROLE_UPDATE = "hasAuthority('ROLE_UPDATE')";
    public static final String ROLE_DELETE = "hasAuthority('ROLE_DELETE')";
    public static final String ROLE_VIEW_DETAIL = "hasAuthority('ROLE_VIEW_DETAIL')";
    public static final String ROLE_VIEW_ALL = "hasAuthority('ROLE_VIEW_ALL')";

    // ================= 7. USERS =================
    public static final String USER_CREATE = "hasAuthority('USER_CREATE')";
    public static final String USER_UPDATE = "hasAuthority('USER_UPDATE') and @customSecurity.isUserOwner(#id)";
    public static final String USER_DELETE = "hasAuthority('USER_DELETE') and @customSecurity.isUserOwner(#id)";
    public static final String USER_VIEW_DETAIL = "hasAuthority('USER_VIEW_DETAIL') and @customSecurity.isUserOwner(#id)";
    public static final String USER_VIEW_ALL = "hasAuthority('USER_VIEW_ALL')";
    public static final String USER_VIEW_DISABLE = "hasAuthority('USER_VIEW_DISABLE')";
    public static final String USER_RESTORE = "hasAuthority('USER_RESTORE')";

    // ================= 8. SUBSCRIBERS =================
    public static final String SUBSCRIBER_CREATE = "hasAuthority('SUBSCRIBER_CREATE')";
    public static final String SUBSCRIBER_UPDATE = "hasAuthority('SUBSCRIBER_UPDATE')";
    public static final String SUBSCRIBER_DELETE = "hasAuthority('SUBSCRIBER_DELETE')";
    public static final String SUBSCRIBER_VIEW_DETAIL = "hasAuthority('SUBSCRIBER_VIEW_DETAIL')";
    public static final String SUBSCRIBER_VIEW_ALL = "hasAuthority('SUBSCRIBER_VIEW_ALL')";

    // ================= 9. FILES =================
    public static final String FILE_UPLOAD = "hasAuthority('FILE_UPLOAD')";
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\constant\SuccessCode.java =====
package com.caochung.recruitment.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessCode {
    GET_SUCCESS(HttpStatus.OK, "Get Success"),
    CREATED_SUCCESS(HttpStatus.CREATED,"Created success"),
    PUT_SUCCESS(HttpStatus.OK,"Put Success"),
    DELETE_SUCCESS(HttpStatus.OK,"Delete Success"),
    RESTORE_SUCCESS(HttpStatus.OK,"Restore Success"),
    VERIFICATION_SUCCESS(HttpStatus.OK,"Verification Success"),

    LOGIN_SUCCESS(HttpStatus.CREATED, "Login Success"),
    LOGOUT_SUCCESS(HttpStatus.CREATED, "Logout Success"),

    UPLOAD_SUCCESS(HttpStatus.CREATED, "Upload Success"),;

    private final HttpStatus status;
    private final String message;

    SuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\constant\UserStatusEnum.java =====
package com.caochung.recruitment.constant;

public enum UserStatusEnum {
    ACTIVE,
    INACTIVE,
    DISABLED,
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\controller\AuthController.java =====
package com.caochung.recruitment.controller;

import com.caochung.recruitment.config.CustomUserDetails;
import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.*;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.LoginResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.service.AuthService;
import com.caochung.recruitment.service.RedisService;
import com.caochung.recruitment.service.UserService;
import com.caochung.recruitment.service.mapper.RoleMapper;
import com.caochung.recruitment.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Authentication & Authorization", description = "APIs for user authentication, registration, and token management.")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final AuthService authService;
    private final RoleMapper roleMapper;
    private final RedisService redisService;

    @Value("${caochung.jwt.access-token-validity-in-second}")
    private long accessTokenExpiration;
    @Value("${caochung.jwt.refresh-token-validity-in-second}")
    private long refreshTokenExpiration;

    @Operation(summary = "User Login", description = "Authenticates a user with username and password, returning access and refresh tokens. Sets refresh token as an HTTP-only cookie.")
    @PostMapping("/auth/login")
    public ResponseEntity<ResponseData<LoginResponseDTO>> Login(@Valid @RequestBody LoginDTO loginDTO) {
        //Nạp input vào Security
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
        //Xác thực người dùng
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //Tạo token
        String accessToken = this.securityUtil.createAccessToken(authentication);

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .userInfo(LoginResponseDTO.UserInfo.builder()
                        .id(customUserDetails.getId())
                        .username(customUserDetails.getName())
                        .email(customUserDetails.getEmail())
                        .role(roleMapper.toDTO(customUserDetails.getRole()))
                        .build())
                .build();
        loginResponseDTO.setAccessToken(accessToken);
        String refreshToken = this.securityUtil.createRefreshToken(authentication);
        this.userService.updateUserToken(refreshToken, customUserDetails.getEmail());

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
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_ACCESS_TOKEN));
        this.userService.updateUserToken(null, email);

        Optional<String> currentUserJWT = SecurityUtil.getCurrentUserJWT();
        currentUserJWT.ifPresent(s -> redisService.createBacklistToken(currentUserJWT.get(), accessTokenExpiration));

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

        User user = this.userService.getUserByUsername(email);
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
        // Giải mã JWT cũ để lấy email
        Jwt decodeRefreshToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodeRefreshToken.getSubject();

        User user = this.userService.getUserByRefreshTokenAndEmail(refreshToken, email);
        if (user == null) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if(user.getRole()!=null && user.getRole().getPermissions()!=null) {
            authorities = user.getRole().getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getName())).toList();
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(), user.getName(), user.getRole(), authorities);

        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, authorities);
        String accessToken = this.securityUtil.createAccessToken(authentication);
        String newRefreshToken = this.securityUtil.createRefreshToken(authentication);

        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .accessToken(accessToken)
                .userInfo(LoginResponseDTO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getName())
                        .email(user.getEmail())
                        .role(roleMapper.toDTO(user.getRole()))
                        .build())
                .build();
        this.userService.updateUserToken(newRefreshToken, email);

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
        UserResponseDTO userResponseDTO = this.authService.registerAndSendOtp(registerDTO);
        return ResponseData.success(userResponseDTO
                , SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Verify OTP", description = "Verifies the OTP sent to the candidate's email to activate the account.")
    @PostMapping("/auth/verify-otp")
    public ResponseData<?> verifyOtp(@RequestBody VerifyOtpDTO verifyOtpDTO) {
        this.authService.VerifyOtp(verifyOtpDTO);
        return ResponseData.success(SuccessCode.VERIFICATION_SUCCESS);
    }

    @Operation(summary = "Forgot Password", description = "Initiates the password reset process. An OTP will be sent to the user's registered email address.")
    @PostMapping("auth/forgot-password")
    public ResponseData<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO email) {
        authService.forgotPassword(email.getEmail());
        return ResponseData.success("The OTP code has been sent to your email", SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Reset Password", description = "Resets the user's password using a valid OTP and the new password provided.")
    @PutMapping("auth/reset-password")
    public ResponseData<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO) {
        authService.resetPassword(resetPasswordRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\controller\CloudinaryController.java =====
package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "File Upload", description = "APIs for uploading files to Cloudinary storage.")
@RequiredArgsConstructor
public class CloudinaryController {
    private final CloudinaryService cloudinaryService;

    @Operation(summary = "Upload multiple files", description = "Uploads an array of files to Cloudinary. Requires 'FILE_UPLOAD' permission.")
    @PostMapping("/uploads")
    @PreAuthorize(SecurityConstant.FILE_UPLOAD)
    public ResponseData<?> uploadFiles(@RequestParam("files") MultipartFile[] files) throws IOException {
        List<String> urls = cloudinaryService.uploadFiles(files);
        return ResponseData.success(urls ,SuccessCode.UPLOAD_SUCCESS);
    }

    @Operation(summary = "Upload a single file", description = "Uploads a single file to Cloudinary. Requires 'FILE_UPLOAD' permission.")
    @PostMapping("/upload")
    @PreAuthorize(SecurityConstant.FILE_UPLOAD)
    public ResponseData<?> uploadFile(@RequestParam("file") MultipartFile file){
        String url = cloudinaryService.uploadFile(file);
        return ResponseData.success(url ,SuccessCode.UPLOAD_SUCCESS);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\controller\CompanyController.java =====
package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.service.CompanyService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Company Management", description = "APIs for managing company profiles and information.")
public class CompanyController {
    private final CompanyService companyService;

    @Operation(summary = "Create a new company", description = "Registers a new company profile with the provided details. Requires 'COMPANY_CREATE' permission.")
    @PostMapping("/companies")
    @PreAuthorize(SecurityConstant.COMPANY_CREATE)
    public ResponseData<CompanyResponseDTO> createCompany(
            @Valid @RequestBody CompanyRequestDTO companyRequestDTO) {
        CompanyResponseDTO company = this.companyService.createCompany(companyRequestDTO);
        return ResponseData.success(company, SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Get company by ID", description = "Fetches detailed information for a specific company using its unique identifier. Accessible to all authenticated users.")
    @GetMapping("/companies/{id}")
    public ResponseData<CompanyResponseDTO> getCompanyById(@PathVariable Long id) {
        CompanyResponseDTO company = this.companyService.getCompanyById(id);
        return ResponseData.success(company, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get all companies", description = "Retrieves a paginated list of all companies, with optional filtering. Accessible to all authenticated users.")
    @GetMapping("/companies")
    public ResponseData<PaginationResponseDTO> getAllCompanies(
            @Filter Specification<Company> specification,
            Pageable pageable) {

        PaginationResponseDTO resultPagination= this.companyService.getAllCompanies(specification, pageable);
        return ResponseData.success(resultPagination, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Update an existing company", description = "Modifies the details of an existing company identified by its ID. Requires 'COMPANY_UPDATE' permission.")
    @PutMapping("/companies/{id}")
    @PreAuthorize(SecurityConstant.COMPANY_UPDATE)
    public ResponseData<?> updateCompany(@PathVariable Long id ,@Valid @RequestBody CompanyRequestDTO companyRequestDTO) {
        this.companyService.updateCompany(id, companyRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a company", description = "Removes a company profile permanently using its unique identifier. Requires 'COMPANY_DELETE' permission.")
    @DeleteMapping("/companies/{id}")
    @PreAuthorize(SecurityConstant.COMPANY_DELETE)
    public ResponseData<?> deleteCompany(@PathVariable Long id) {
        this.companyService.deleteCompany(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }

    @Operation(summary = "Get all inactive companies", description = "Retrieves a paginated list of all inactive companies. Requires 'COMPANY_VIEW_INACTIVE' permission.")
    @GetMapping("/companies/trash")
    @PreAuthorize(SecurityConstant.COMPANY_VIEW_INACTIVE)
    public ResponseData<PaginationResponseDTO> getAllInactiveCompanies(Pageable pageable) {
        PaginationResponseDTO resultPagination= this.companyService.getAllInactiveCompanies(pageable);
        return ResponseData.success(resultPagination, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Restore a company", description = "Restore a company. Requires 'COMPANY_RESTORE' permission.")
    @PutMapping("/companies/{id}/restore")
    @PreAuthorize(SecurityConstant.COMPANY_RESTORE)
    public ResponseData<?> restoreUser(@PathVariable Long id) {
        this.companyService.restoreCompanyById(id);
        return ResponseData.success(SuccessCode.RESTORE_SUCCESS);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\controller\JobController.java =====
package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.dto.request.JobRequestDTO;
import com.caochung.recruitment.dto.response.JobResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.service.JobService;
import com.caochung.recruitment.service.RedisService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Job Management", description = "APIs for managing job postings and listings.")
public class JobController {
    private final JobService jobService;
    private final RedisService redisService;

    @Operation(summary = "Get all jobs", description = "Retrieves a paginated list of all available job postings, with optional filtering. Accessible to all authenticated users.")
    @GetMapping("jobs")
    public ResponseData<PaginationResponseDTO> getJobs(
            @Filter Specification<Job> specification,
            Pageable pageable) {
        return ResponseData.success(this.jobService.getJobs(specification, pageable, false), SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get jobs by company", description = "Retrieves a paginated list of job postings associated with the current user's company, with optional filtering. Requires authentication.")
    @GetMapping("jobs/company")
    public ResponseData<PaginationResponseDTO> getJobsCompany(
            @Filter Specification<Job> specification,
            Pageable pageable) {
        return ResponseData.success(this.jobService.getJobs(specification, pageable, true), SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get job by ID", description = "Fetches detailed information for a specific job posting using its unique identifier. Accessible to all authenticated users.")
    @GetMapping("/jobs/{id}")
    public ResponseData<JobResponseDTO> getJobById(@PathVariable Long id) {
        redisService.countViewJob(id);
        return ResponseData.success(this.jobService.getJobById(id), SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Create a new job posting", description = "Creates a new job posting with the provided details. Requires 'JOB_CREATE' permission.")
    @PostMapping("jobs")
    @PreAuthorize(SecurityConstant.JOB_CREATE)
    public ResponseData<JobResponseDTO> createJob(@Valid @RequestBody JobRequestDTO job) {
        return ResponseData.success(this.jobService.createJob(job), SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Update an existing job posting", description = "Modifies the details of an existing job posting identified by its ID. Requires 'JOB_UPDATE' permission.")
    @PutMapping("jobs/{id}")
    @PreAuthorize(SecurityConstant.JOB_UPDATE)
    public ResponseData<?> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequestDTO job) {
        this.jobService.updateJob(id, job);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a job posting", description = "Removes a job posting permanently using its unique identifier. Requires 'JOB_DELETE' permission.")
    @DeleteMapping("jobs/{id}")
    @PreAuthorize(SecurityConstant.JOB_DELETE)
    public ResponseData<?> deleteJob(@PathVariable Long id) {
        this.jobService.deleteJob(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }

    @Operation(summary = "Get Top 10 Trending Jobs")
    @GetMapping("/jobs/trending")
    public ResponseData<List<JobResponseDTO>> getTrendingJobs() {
        Set<String> topJobIds = redisService.getJobTrendings();
        if (topJobIds == null || topJobIds.isEmpty()) {
            return ResponseData.success(new ArrayList<>(), SuccessCode.GET_SUCCESS);
        }

        List<Long> ids = topJobIds.stream().map(Long::valueOf).toList();

        List<JobResponseDTO> trendingJobs = new ArrayList<>();

        for (Long id : ids) {
            try {
                JobResponseDTO job = jobService.getJobById(id);
                trendingJobs.add(job);
            } catch (Exception e) {
                System.out.println("Skip Trending Job ID " + id);
            }
        }

        return ResponseData.success(trendingJobs, SuccessCode.GET_SUCCESS);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\controller\PermissionController.java =====
package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.dto.request.PermissionRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.PermissionResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.repository.PermissionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.caochung.recruitment.service.PermissionService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.FilteredEndpoint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "APIs for managing system permissions.")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Create a new permission", description = "Creates a new permission entry in the system. Requires 'PERMISSION_CREATE' permission.")
    @PostMapping("/permissions")
    @PreAuthorize(SecurityConstant.PERMISSION_CREATE)
    public ResponseData<PermissionResponseDTO> createPermission(@Valid @RequestBody PermissionRequestDTO permissionRequestDTO) {
        PermissionResponseDTO responseDTO = this.permissionService.createPermission(permissionRequestDTO);
        return ResponseData.success(responseDTO, SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Get all permissions", description = "Retrieves a paginated list of all permissions, with optional filtering. Requires 'PERMISSION_VIEW_ALL' permission.")
    @GetMapping("/permissions")
    @PreAuthorize(SecurityConstant.PERMISSION_VIEW_ALL)
    public ResponseData<PaginationResponseDTO> getPermissions(
            @Filter Specification<Permission> specification,
            Pageable pageable){
        PaginationResponseDTO  paginationResponseDTO = this.permissionService.getPermissions(specification,pageable);
        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get permission by ID", description = "Fetches detailed information for a specific permission using its unique identifier. Requires 'PERMISSION_VIEW_DETAIL' permission.")
    @GetMapping("/permissions/{id}")
    @PreAuthorize(SecurityConstant.PERMISSION_VIEW_DETAIL)
    public ResponseData<PermissionResponseDTO> getPermissionById(@PathVariable Long id){
        PermissionResponseDTO responseDTO = this.permissionService.getPermissionById(id);
        return ResponseData.success(responseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Update an existing permission", description = "Modifies the details of an existing permission identified by its ID. Requires 'PERMISSION_UPDATE' permission.")
    @PutMapping("/permissions/{id}")
    @PreAuthorize(SecurityConstant.PERMISSION_UPDATE)
    public ResponseData<?> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionRequestDTO permissionRequestDTO){
        this.permissionService.updatePermission(id, permissionRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a permission", description = "Removes a permission permanently using its unique identifier. Requires 'PERMISSION_DELETE' permission.")
    @DeleteMapping("/permissions/{id}")
    @PreAuthorize(SecurityConstant.PERMISSION_DELETE)
    public ResponseData<?> deletePermission(@PathVariable Long id){
        this.permissionService.deletePermission(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\controller\ResumeController.java =====
package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.dto.request.ResumeRequestDTO;
import com.caochung.recruitment.dto.request.ResumeUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.ResumeResponseDTO;
import com.caochung.recruitment.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Resume Management", description = "APIs for managing user resumes.")
public class ResumeController {
    private final ResumeService resumeService;

    @Operation(summary = "Get all resumes", description = "Retrieves a paginated list of all resumes, with optional filtering. Requires 'RESUME_VIEW_ALL' permission.")
    @GetMapping("/resumes")
    @PreAuthorize(SecurityConstant.RESUME_VIEW_ALL)
    public ResponseData<PaginationResponseDTO> getResumes(
            @Filter Specification<Resume> specification,
            Pageable pageable){
        PaginationResponseDTO paginationResponseDTO = this.resumeService.getResumes(specification,pageable);
        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Create a new resume", description = "Creates a new resume entry for the authenticated user. Requires 'RESUME_CREATE' permission.")
    @PostMapping(value = "/resumes")
    @PreAuthorize(SecurityConstant.RESUME_CREATE)
    public ResponseData<ResumeResponseDTO> createResume(@Valid @RequestBody ResumeRequestDTO resumeRequestDTO){
        ResumeResponseDTO resumeResponseDTO = this.resumeService.submitResume(resumeRequestDTO);
        return ResponseData.success(resumeResponseDTO, SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Update an existing resume", description = "Modifies the details of an existing resume identified by its ID. Requires 'RESUME_UPDATE' permission.")
    @PatchMapping("/resumes/{id}")
    @PreAuthorize(SecurityConstant.RESUME_UPDATE)
    public ResponseData<?> updateResume(@PathVariable Long id, @Valid @RequestBody ResumeUpdateDTO resumeUpdateDTO){
        this.resumeService.updateResume(id, resumeUpdateDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a resume", description = "Removes a resume permanently using its unique identifier. Requires 'RESUME_DELETE' permission.")
    @DeleteMapping("/resumes/{id}")
    @PreAuthorize(SecurityConstant.RESUME_DELETE)
    public ResponseData<?> deleteResume(@PathVariable Long id){
        this.resumeService.deleteResume(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }

    @Operation(summary = "Get resume by ID", description = "Fetches detailed information for a specific resume using its unique identifier. Requires 'RESUME_VIEW_DETAIL' permission.")
    @GetMapping("/resumes/{id}")
    @PreAuthorize(SecurityConstant.RESUME_VIEW_DETAIL)
    public ResponseData<ResumeResponseDTO> getResumeById(@PathVariable Long id){
        ResumeResponseDTO resumeResponseDTO = this.resumeService.getResumeById(id);
        return ResponseData.success(resumeResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get resumes by current user", description = "Retrieves a paginated list of resumes belonging to the currently authenticated user. Requires 'RESUME_VIEW_OWN' permission.")
    @GetMapping("/resumes/by-user")
    @PreAuthorize(SecurityConstant.RESUME_VIEW_OWN)
    public ResponseData<PaginationResponseDTO> getResumesByUser(Pageable pageable){
        PaginationResponseDTO responseDTO = this.resumeService.getResumeByUser(pageable);
        return ResponseData.success(responseDTO, SuccessCode.GET_SUCCESS);
    }

//    @GetMapping("/resumes/by-company")
//    @PreAuthorize(SecurityConstant.RESUME_VIEW_COMPANY)
//    public ResponseData<PaginationResponseDTO> getResumesByCompany(
//            @Filter Specification<Resume> specification,
//            Pageable pageable){
//        PaginationResponseDTO paginationResponseDTO = this.resumeService.getResumeByCompany(specification,pageable);
//        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
//    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\controller\RoleController.java =====
package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.RoleRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.RoleResponseDTO;
import com.caochung.recruitment.service.RoleService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing user roles and their permissions.")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Create a new role", description = "Creates a new role with specified permissions. Requires 'ROLE_CREATE' permission.")
    @PostMapping("/roles")
    @PreAuthorize(SecurityConstant.ROLE_CREATE)
    public ResponseData<RoleResponseDTO> createRole(@Valid @RequestBody RoleRequestDTO roleRequestDTO) {
        RoleResponseDTO responseDTO = this.roleService.createRole(roleRequestDTO);
        return ResponseData.success(responseDTO, SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Get all roles", description = "Retrieves a paginated list of all roles, with optional filtering. Requires 'ROLE_VIEW_ALL' permission.")
    @GetMapping("/roles")
    @PreAuthorize(SecurityConstant.ROLE_VIEW_ALL)
    public ResponseData<PaginationResponseDTO> getRoles(
            @Filter Specification<Role> specification,
            Pageable pageable){
        PaginationResponseDTO  paginationResponseDTO = this.roleService.getRoles(specification,pageable);
        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get role by ID", description = "Fetches detailed information for a specific role using its unique identifier. Requires 'ROLE_VIEW_DETAIL' permission.")
    @GetMapping("/roles/{id}")
    @PreAuthorize(SecurityConstant.ROLE_VIEW_DETAIL)
    public ResponseData<RoleResponseDTO> getRoleById(@PathVariable Long id){
        RoleResponseDTO responseDTO = this.roleService.getRoleById(id);
        return ResponseData.success(responseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Update an existing role", description = "Modifies the details and permissions of an existing role identified by its ID. Requires 'ROLE_UPDATE' permission.")
    @PutMapping("/roles/{id}")
    @PreAuthorize(SecurityConstant.ROLE_UPDATE)
    public ResponseData<?> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequestDTO roleRequestDTO){
        this.roleService.updateRole(id, roleRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a role", description = "Removes a role permanently using its unique identifier. Requires 'ROLE_DELETE' permission.")
    @DeleteMapping("/roles/{id}")
    @PreAuthorize(SecurityConstant.ROLE_DELETE)
    public ResponseData<?> deleteRole(@PathVariable Long id){
        this.roleService.deleteRole(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\controller\SkillController.java =====
package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.dto.request.SkillRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.SkillResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.caochung.recruitment.repository.SkillRepository;
import com.caochung.recruitment.service.SkillService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Skill Management", description = "APIs for managing skills.")
public class SkillController {
    private final SkillService skillService;

    @Operation(summary = "Get all skills", description = "Retrieves a paginated list of all skills, with optional filtering. Accessible to all authenticated users.")
    @GetMapping("skills")
    public ResponseData<PaginationResponseDTO> getSkills(
            @Filter Specification<Skill> specification,
            Pageable pageable) {
        return ResponseData.success(this.skillService.getSkills(specification, pageable), SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Create a new skill", description = "Creates a new skill entry in the system. Requires 'SKILL_CREATE' permission.")
    @PostMapping("skills")
    @PreAuthorize(SecurityConstant.SKILL_CREATE)
    public ResponseData<SkillResponseDTO> createSkill(@Valid @RequestBody SkillRequestDTO skillRequestDTO) {
        return ResponseData.success(this.skillService.createSkill(skillRequestDTO), SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Get skill by ID", description = "Fetches detailed information for a specific skill using its unique identifier. Accessible to all authenticated users.")
    @GetMapping("/skills/{id}")
    public ResponseData<SkillResponseDTO> getSkillById(@PathVariable Long id) {
        return ResponseData.success(this.skillService.getSkillById(id), SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Update an existing skill", description = "Modifies the details of an existing skill identified by its ID. Requires 'SKILL_UPDATE' permission.")
    @PutMapping("skills/{id}")
    @PreAuthorize(SecurityConstant.SKILL_UPDATE)
    public ResponseData<?> updateSkill(@PathVariable Long id, @Valid @RequestBody SkillRequestDTO skill) {
        this.skillService.updateSkill(id, skill);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a skill", description = "Removes a skill permanently using its unique identifier. Requires 'SKILL_DELETE' permission.")
    @DeleteMapping("skills/{id}")
    @PreAuthorize(SecurityConstant.SKILL_DELETE)
    public ResponseData<?> deleteSkill(@PathVariable Long id) {
        this.skillService.deleteSkill(id);
        return  ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\controller\SubscriberController.java =====
package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.dto.request.SubscriberRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.SubscriberResponseDTO;
import com.caochung.recruitment.service.SubscriberService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Subscriber Management", description = "APIs for managing email subscribers and notifications.")
public class SubscriberController {
    private final SubscriberService subscriberService;

    @Operation(summary = "Create a new subscriber", description = "Registers a new email subscriber. Requires 'SUBSCRIBER_CREATE' permission.")
    @PostMapping("/subscribers")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_CREATE)
    public ResponseData<SubscriberResponseDTO> createSubscriber(@Valid @RequestBody SubscriberRequestDTO subscriberRequestDTO){
        SubscriberResponseDTO subscriberResponseDTO = subscriberService.createSubscriber(subscriberRequestDTO);
        return ResponseData.success(subscriberResponseDTO ,SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Get all subscribers", description = "Retrieves a paginated list of all subscribers, with optional filtering. Accessible to all authenticated users.")
    @GetMapping("/subscribers")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_VIEW_ALL)
    public ResponseData<PaginationResponseDTO> getAllSubscribers(
            @Filter Specification<Subscriber> specification,
            Pageable pageable) {

        PaginationResponseDTO resultPagination= this.subscriberService.getAllSubscriber(specification, pageable);
        return ResponseData.success(resultPagination, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get subscriber by ID", description = "Fetches detailed information for a specific subscriber using their unique identifier. Requires 'SUBSCRIBER_VIEW_DETAIL' permission.")
    @GetMapping("/subscribers/{id}")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_VIEW_DETAIL)
    public ResponseData<SubscriberResponseDTO> getSubscriberById(@PathVariable Long id){
        SubscriberResponseDTO subscriberResponseDTO = subscriberService.getSubscriberById(id);
        return ResponseData.success(subscriberResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Update an existing subscriber", description = "Modifies the details of an existing subscriber identified by their ID. Requires 'SUBSCRIBER_UPDATE' permission.")
    @PutMapping("/subscribers/{id}")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_UPDATE)
    public ResponseData<?> updateSubscriber(@PathVariable Long id ,@Valid @RequestBody SubscriberRequestDTO subscriberRequestDTO){
        subscriberService.updateSubscriber(id, subscriberRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a subscriber", description = "Removes a subscriber permanently using their unique identifier. Requires 'SUBSCRIBER_DELETE' permission.")
    @DeleteMapping("subscribers/{id}")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_DELETE)
    public ResponseData<?> deleteSubscriber(@PathVariable Long id){
        subscriberService.deleteSubscriber(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\controller\UserController.java =====
package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.UserRequestDTO;
import com.caochung.recruitment.dto.request.UserUpdateDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.service.UserService;
import com.caochung.recruitment.service.impl.UserServiceImpl;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing user accounts and profiles.")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Create a new user", description = "Registers a new user account with the provided details. Requires 'USER_CREATE' permission.")
    @PostMapping("/users")
    @PreAuthorize(SecurityConstant.USER_CREATE)
    public ResponseData<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO user) {
        UserResponseDTO userResponseDTO=this.userService.createUser(user);
        return ResponseData.success(userResponseDTO ,SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users, with optional filtering. Requires 'USER_VIEW_ALL' permission.")
    @GetMapping("/users")
    @PreAuthorize(SecurityConstant.USER_VIEW_ALL)
    public ResponseData<PaginationResponseDTO> getAllUsers(
            @Filter Specification<User> specification, Pageable pageable) {
        PaginationResponseDTO paginationResponseDTO = this.userService.getAllUsers(specification, pageable);
        return ResponseData.success(paginationResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get user by ID", description = "Fetches detailed information for a specific user using their unique identifier. Requires 'USER_VIEW_DETAIL' permission.")
    @GetMapping("/users/{id}")
    @PreAuthorize(SecurityConstant.USER_VIEW_DETAIL)
    public ResponseData<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO userResponseDTO = this.userService.getUserById(id);
        return ResponseData.success(userResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Update an existing user", description = "Modifies the details of an existing user identified by their ID. Requires 'USER_UPDATE' permission.")
    @PutMapping("/users/{id}")
    @PreAuthorize(SecurityConstant.USER_UPDATE)
    public ResponseData<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO user) {
        this.userService.updateUser(id, user);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a user", description = "Removes a user account permanently using their unique identifier. Requires 'USER_DELETE' permission.")
    @DeleteMapping("/users/{id}")
    @PreAuthorize(SecurityConstant.USER_DELETE)
    public ResponseData<?> deleteUser(@PathVariable Long id) {
        this.userService.deleteUser(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }

    @Operation(summary = "Get all disable users", description = "Retrieves a paginated list of all disable users. Requires 'USER_VIEW_DISABLE' permission.")
    @GetMapping("/users/trash")
    @PreAuthorize(SecurityConstant.USER_VIEW_DISABLE)
    public ResponseData<PaginationResponseDTO> getAllDisableUsers(Pageable pageable) {
        PaginationResponseDTO resultPagination= this.userService.getAllDisableUser(pageable);
        return ResponseData.success(resultPagination, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Restore a user", description = "Restore a user account. Requires 'USER_RESTORE' permission.")
    @PutMapping("/users/{id}/restore")
    @PreAuthorize(SecurityConstant.USER_RESTORE)
    public ResponseData<?> restoreUser(@PathVariable Long id) {
        this.userService.restoreUserById(id);
        return ResponseData.success(SuccessCode.RESTORE_SUCCESS);
    }

}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\domain\Base.java =====
package com.caochung.recruitment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

@Getter @Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Base implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;


}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\domain\Company.java =====
package com.caochung.recruitment.domain;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Table(name = "companies")
@Getter @Setter
@SQLDelete(sql = "UPDATE companies SET status = 'INACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "status != 'INACTIVE'")
public class Company extends Base {
    @Column(name = "name")
    private String name;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "logo")
    private String logo;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CompanyStatusEnum status;

    @OneToMany(mappedBy = "company",  fetch = FetchType.LAZY)
    private List<User> users;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<Job> jobs;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\domain\Job.java =====
package com.caochung.recruitment.domain;

import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.constant.LevelEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "jobs")
@Getter @Setter
@SQLDelete(sql = "UPDATE jobs SET active = 'INACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "active != 'INACTIVE'")
public class Job extends Base{
    @Column(name = "name")
    private String name;

    @Column(name = "location")
    private String location;

    @Column(name = "salary")
    private Double salary;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "level")
    @Enumerated(EnumType.STRING)
    private LevelEnum level;

    @Column(name = "description", columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "active")
    @Enumerated(EnumType.STRING)
    private JobStatusEnum active;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "job_skill",
    joinColumns = @JoinColumn(name = "job_id"),
    inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> skills;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    private List<Resume> resumes ;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\domain\Permission.java =====
package com.caochung.recruitment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "permissions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends Base{
    @Column(name = "name")
    private String name;

    @Column(name = "api_path")
    private String apiPath;

    @Column(name = "method")
    private String method;

    @Column(name = "module")
    private String module;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private List<Role> roles;

    public Permission(String name, String apiPath, String method, String module) {
        this.name = name;
        this.apiPath = apiPath;
        this.method = method;
        this.module = module;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\domain\Resume.java =====
package com.caochung.recruitment.domain;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.dto.response.ResponseData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.WhereJoinTable;

@Entity
@Table(name = "resumes", uniqueConstraints = @UniqueConstraint(columnNames = {"email", "job_id"}))
@Getter @Setter
@SQLDelete(sql = "UPDATE resumes SET status = 'SYSTEM_CANCEL' WHERE id = ?")
//@Where(clause = "status != 'SYSTEM_CANCEL'")
public class Resume extends Base{
    @Column(name = "email")
    private String email;

    @Column(name = "url")
    private String url;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ResumeStatusEnum status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\domain\Role.java =====
package com.caochung.recruitment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter @Setter
public class Role extends Base{
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private boolean active;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "permission_role", joinColumns = @JoinColumn(name = "role_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id"), uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id"}))
    private Set<Permission> permissions;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "role")
    private List<User> users;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\domain\Skill.java =====
package com.caochung.recruitment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "skills")
@Setter @Getter
public class Skill extends Base{
    @Column(name = "name")
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "skills")
    private List<Job> jobs;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "skills")
    private List<Subscriber> subscribers;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\domain\Subscriber.java =====
package com.caochung.recruitment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "subscriber")
@Getter @Setter
public class Subscriber extends Base{
    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "subscriber_skill",
    joinColumns = @JoinColumn(name = "subscriber_id"),
    inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> skills;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\domain\User.java =====
package com.caochung.recruitment.domain;

import com.caochung.recruitment.constant.GenderEnum;
import com.caochung.recruitment.constant.UserStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Table(name="users")
@Getter
@Setter
@SQLDelete(sql = "UPDATE users SET status = 'DISABLED', updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "status != 'DISABLED'")
public class User extends Base{
    @Column(name = "name")
    private  String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "age")
    private int age;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    @Column(name = "address")
    private String address;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatusEnum status;

    @Column(name = "refresh_token",columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Resume> resumes;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\CompanyRequestDTO.java =====
package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.util.annotation.CompanyStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.CompanyStatusEnum.*;

@Getter @Setter
@Builder
public class CompanyRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String description;

    private String address;

    private String logo;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\ForgotPasswordRequestDTO.java =====
package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ForgotPasswordRequestDTO {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\JobRequestDTO.java =====
package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.constant.LevelEnum;
import com.caochung.recruitment.util.annotation.JobStatus;
import com.caochung.recruitment.util.annotation.Level;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

import static com.caochung.recruitment.constant.JobStatusEnum.*;
import static com.caochung.recruitment.constant.LevelEnum.*;

@Getter
@Setter
public class JobRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Location cannot be empty")
    private String location;

    private Double salary;

    private Integer quantity;

    @Level(anyOf = {FRESHER, INTERN, JUNIOR, MIDDLE, SENIOR, LEAD})
    private String level;

    private String description;

    private Instant startDate;

    private Instant endDate;

    @JobStatus (anyOf = {OPEN, CLOSED, DRAFT, FILLED})
    private String active;

    @NotNull(message = "Company Id cannot be null")
    private Long companyId;

    @NotEmpty(message = "Skills cannot be empty")
    private List<Long> skills;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\LoginDTO.java =====
package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginDTO {
    @NotBlank(message = "Username cannot be empty")
    private String username;
    @NotBlank(message = "Password cannot be empty")
    private String password;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\PermissionRequestDTO.java =====
package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.domain.Role;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PermissionRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Api path cannot be empty")
    private String apiPath;

    @NotBlank(message = "Method cannot be empty")
    @Pattern(regexp = "POST|GET|PUT|PATCH|DELETE", message = "Method invalid")
    private String method;

    @NotBlank(message = "Module cannot be empty")
    private String module;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\RegisterDTO.java =====
package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.util.annotation.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.GenderEnum.*;

@Getter
@Setter
@Builder
public class RegisterDTO {
    @NotBlank(message = "Username cannot be empty")
    private  String name;

    @Email(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    private int age;

    @Gender(anyOf = {MALE, FEMALE, OTHER})
    private String gender;

    private String address;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\ResetPasswordRequestDTO.java =====
package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class ResetPasswordRequestDTO {
    @NotBlank(message = "Email cannot be empty")
    private String email;
    @NotBlank(message = "OTP cannot be empty")
    private String otp;
    @NotBlank(message = "New password cannot be empty")
    private String newPassword;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\ResumeRequestDTO.java =====
package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.util.annotation.ResumeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.ResumeStatusEnum.*;

@Getter @Setter
@Builder
public class ResumeRequestDTO {
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "CV cannot be empty")
    private String url;

    @ResumeStatus(anyOf = {PENDING, REVIEWING, APPROVED, REJECTED})
    private String status;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Job ID cannot be null")
    private Long jobId;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\ResumeUpdateDTO.java =====
package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.util.annotation.ResumeStatus;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.ResumeStatusEnum.*;
import static com.caochung.recruitment.constant.ResumeStatusEnum.REJECTED;

@Getter @Setter
public class ResumeUpdateDTO {
    @ResumeStatus(anyOf = {PENDING, REVIEWING, APPROVED, REJECTED})
    private String status;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\RoleRequestDTO.java =====
package com.caochung.recruitment.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class RoleRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String description;

    private boolean active;

    private List<Long> permissions;

}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\SkillRequestDTO.java =====
package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SkillRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\SubscriberRequestDTO.java =====
package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubscriberRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Email(message = "Email is not in the correct format.")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotEmpty(message = "Skill cannot be empty")
    private List<Long> skills;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\UserRequestDTO.java =====
package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.constant.GenderEnum;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.util.annotation.Gender;
import com.caochung.recruitment.util.annotation.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import static com.caochung.recruitment.constant.GenderEnum.FEMALE;
import static com.caochung.recruitment.constant.GenderEnum.MALE;
import static com.caochung.recruitment.constant.GenderEnum.OTHER;
import static com.caochung.recruitment.constant.UserStatusEnum.*;

@Getter
@Setter
public class UserRequestDTO implements Serializable {
    @NotBlank(message = "Username cannot be empty")
    private  String name;

    @Email(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    private int age;

    @Gender(anyOf = {MALE, FEMALE, OTHER})
    private String gender;

    private String address;

    private Long companyId;

    @NotNull(message = "Role ID cannot be empty")
    private Long role;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\UserUpdateDTO.java =====
package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.util.annotation.Gender;
import com.caochung.recruitment.util.annotation.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.GenderEnum.*;
import static com.caochung.recruitment.constant.UserStatusEnum.*;

@Getter @Setter
public class UserUpdateDTO {
    private  String name;

    private int age;

    @Gender(anyOf = {MALE, FEMALE, OTHER})
    private String gender;

    private String address;

    private Long companyId;

    private Long role;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\request\VerifyOtpDTO.java =====
package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpDTO {
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Otp cannot be empty")
    private String otp;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\CompanyResponseDTO.java =====
package com.caochung.recruitment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
public class CompanyResponseDTO {
    private Long id;

    private String name;

    private String description;

    private String address;

    private String logo;

    private String status;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\JobAlertEmailDTO.java =====
package com.caochung.recruitment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JobAlertEmailDTO {
    private String userName;
    private String jobName;
    private String companyName;
    private Double salary;
    private String location;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\JobResponseDTO.java =====
package com.caochung.recruitment.dto.response;

import com.caochung.recruitment.util.annotation.Level;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter @Setter
public class JobResponseDTO {
    private Long id;

    private String name;

    private String location;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant startDate;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant endDate;

    private String description;

    private Double salary;

    private Integer quantity;

    private String level;

    private String active;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;

    private JobCompany company;

    private List<JobSkill> skills;

    @Getter @Setter
    public static class JobSkill{
        private Long id;
        private String name;
    }

    @Getter @Setter
    public static class JobCompany{
        private Long id;
        private String name;
        private String logo;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\LoginResponseDTO.java =====
package com.caochung.recruitment.dto.response;

import com.caochung.recruitment.domain.Permission;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {
    @JsonProperty("access_token")
    private String accessToken;
    private UserInfo userInfo;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private RoleResponseDTO role;
    }


    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInsideToken {
        private Long id;
        private String username;
        private String email;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\PaginationResponseDTO.java =====
package com.caochung.recruitment.dto.response;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponseDTO {
    private Meta meta;
    private Object result;

    @Getter @Setter
    @Builder
    public static class Meta {
        private int page;
        private int pageSize;
        private long totalItems;
        private int totalPages;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\PermissionResponseDTO.java =====
package com.caochung.recruitment.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter @Setter
public class PermissionResponseDTO {
    private Long id;

    private String name;

    private String apiPath;

    private String method;

    private String module;

    private Instant createdAt;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\ResponseData.java =====
package com.caochung.recruitment.dto.response;

import com.caochung.recruitment.constant.SuccessCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter @Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData<T> {
    private int status;
    private String message;
    private T data;

    public static <T> ResponseData<T> success(T data, SuccessCode successCode) {
        return ResponseData.<T>builder()
                .status(successCode.getStatus().value())
                .message(successCode.getMessage())
                .data(data)
                .build();
    }
    public static <T> ResponseData<T> success(SuccessCode successCode) {
        return ResponseData.<T>builder()
                .status(successCode.getStatus().value())
                .message(successCode.getMessage())
                .build();
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\ResponseError.java =====
package com.caochung.recruitment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ResponseError {
    private Date timestamp;
    private int status;
    private String path;
    private String error;
    private String message;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\ResumeResponseDTO.java =====
package com.caochung.recruitment.dto.response;

import com.caochung.recruitment.util.annotation.Gender;
import com.caochung.recruitment.util.annotation.ResumeStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

import static com.caochung.recruitment.constant.GenderEnum.*;

@Getter @Setter
public class ResumeResponseDTO {
    private Long id;

    private String email;

    private String url;

    private String status;

    private String companyName;

    private UserResume user;

    private JobResume job;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;

    @Getter @Setter
    public static class UserResume {
        private Long id;
        private String name;
    }

    @Getter @Setter
    public static class JobResume {
        private Long id;
        private String name;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\RoleResponseDTO.java =====
package com.caochung.recruitment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter @Setter
public class RoleResponseDTO {
    private Long id;

    private String name;

    private String description;

    private boolean active;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;

    private Set<PermissionRole> permissions;

    @Getter @Setter
    public static class PermissionRole{
        private Long id;
        private String name;
        private String apiPath;
        private String method;
        private String module;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\SkillResponseDTO.java =====
package com.caochung.recruitment.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SkillResponseDTO {
    private String id;
    private String name;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\SubscriberResponseDTO.java =====
package com.caochung.recruitment.dto.response;

import com.caochung.recruitment.domain.Skill;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter @Setter
public class SubscriberResponseDTO {
    private Long id;

    private String name;

    private String email;

    private List<SkillResponseDTO> skills;

    private Instant createdAt;

    private Instant updatedAt;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\dto\response\UserResponseDTO.java =====
package com.caochung.recruitment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {
    private Long id;
    private  String name;
    private String email;
    private int age;
    private String gender;
    private String address;
    private String status;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;
    private CompanyResponseDTO company;
    private RoleResponseDTO role;

    @Getter @Setter
    public static class CompanyResponseDTO {
        private Long id;
        private String name;
    }

    @Getter @Setter
    public static class RoleResponseDTO {
        private Long id;
        private String name;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\event\ForgotPasswordEvent.java =====
package com.caochung.recruitment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class ForgotPasswordEvent {
    private String email;
    private String otpToken;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\event\ResumeStatusUpdateEvent.java =====
package com.caochung.recruitment.event;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Builder
public class ResumeStatusUpdateEvent {
    private String emailTo;
    private String username;
    private String jobName;
    private String companyName;
    private ResumeStatusEnum status;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\event\UserRegisterEvent.java =====
package com.caochung.recruitment.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserRegisterEvent {
    private String email;
    private String otpToken;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\exception\AppException.java =====
package com.caochung.recruitment.exception;

import com.caochung.recruitment.constant.ErrorCode;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException{
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\exception\GlobalExceptionHandler.java =====
package com.caochung.recruitment.exception;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.dto.response.ResponseError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;


import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ResponseError> handleAppException(AppException e, WebRequest request) {
        ErrorCode errorCode = e.getErrorCode();

        ResponseError responseError = new ResponseError();
        responseError.setTimestamp(new Date());
        responseError.setStatus(errorCode.getHttpStatus().value());
        responseError.setPath(request.getDescription(false).replace("uri=", ""));
        responseError.setError(errorCode.name());
        responseError.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(responseError);
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ResponseError> handleLoginException(Exception ex, WebRequest request){
        ResponseError responseError = new ResponseError();
        responseError.setStatus(HttpStatus.UNAUTHORIZED.value());
        responseError.setTimestamp(new Date());
        responseError.setPath(request.getContextPath() + "/login");
        responseError.setMessage(ex.getMessage());
        responseError.setError("Invalid username/password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseError);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        ResponseError res = new ResponseError();
        res.setTimestamp(new Date());
        res.setStatus(HttpStatus.BAD_REQUEST.value());
        res.setPath(request.getDescription(false).replace("uri=", ""));
        res.setError(ex.getBody().getDetail());
        List<String> errors = fieldErrors.stream().map(f-> f.getDefaultMessage()).toList();
        res.setMessage(String.join(", ", errors));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseError> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, WebRequest request){
        ResponseError responseError = new ResponseError();
        responseError.setTimestamp(new Date());
        responseError.setPath(request.getDescription(false).replace("uri=", ""));
        responseError.setStatus(ErrorCode.FILE_TOO_LARGE.getHttpStatus().value());
        responseError.setError(ErrorCode.FILE_TOO_LARGE.name());
        responseError.setMessage(ex.getMessage());
        return ResponseEntity.status(ErrorCode.FILE_TOO_LARGE.getHttpStatus()).body(responseError);
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ResponseError> handleAccessDeniedException(Exception ex, WebRequest request) {
        ResponseError responseError = new ResponseError();
        responseError.setTimestamp(new Date());
        responseError.setPath(request.getDescription(false).replace("uri=", ""));
        responseError.setStatus(HttpStatus.FORBIDDEN.value());
        responseError.setError(ErrorCode.ACCESS_DENIED.name());
        responseError.setMessage(ErrorCode.ACCESS_DENIED.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseError> handleUnwantedException(Exception ex, WebRequest request){
        ResponseError responseError = new ResponseError();
        responseError.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseError.setTimestamp(new Date());
        responseError.setPath(request.getDescription(false).replace("uri=", ""));
        responseError.setError(ErrorCode.UNCATEGORIZED_EXCEPTION.name());
        responseError.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\listener\AuthEventListener.java =====
package com.caochung.recruitment.listener;

import com.caochung.recruitment.event.ForgotPasswordEvent;
import com.caochung.recruitment.event.UserRegisterEvent;
import com.caochung.recruitment.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j(topic = "AUTH-LISTENER")
@RequiredArgsConstructor
public class AuthEventListener {
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegisterEvent(UserRegisterEvent user) {
        log.info("SEND EMAIL TO {}", user.getEmail());
        try {
            emailService.emailVerification(user);
            log.info("SEND EMAIL SUCCESSFULLY");
        }catch (Exception e) {
            log.error("SEND EMAIL FAILED: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleForgotPasswordEvent(ForgotPasswordEvent user) {
        log.info("SEND EMAIL TO {}", user.getEmail());
        try {
            emailService.sendForgotPasswordEmail(user);
            log.info("SEND EMAIL SUCCESSFULLY");
        }catch (Exception e) {
            log.error("SEND EMAIL FAILED: {}", e.getMessage());
        }
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\listener\ResumeEventListener.java =====
package com.caochung.recruitment.listener;

import com.caochung.recruitment.event.ResumeStatusUpdateEvent;
import com.caochung.recruitment.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j(topic = "RESUME-LISTENER")
@RequiredArgsConstructor
public class ResumeEventListener {
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeStatusUpdateEvent(ResumeStatusUpdateEvent resumeStatusUpdateEvent) {
        log.info("SEND EMAIL TO {}", resumeStatusUpdateEvent.getEmailTo());
        try {
            emailService.sendResumeStatusEmail(resumeStatusUpdateEvent);
        }catch (Exception e){
            log.error("ERROR SEND EMAIL UPDATE STATUS RESUME: {}",e.getMessage());
        }
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\repository\CompanyRepository.java =====
package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {
    Boolean existsByName(String name);

    @Query(value = "SELECT * FROM companies WHERE id = :id", nativeQuery = true)
    Optional<Company> findByIdIncludingDeleted(Long id);

    @Modifying
    @Query(value = "UPDATE companies SET status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    void restoreCompanyById(Long id);

    @Query(value = "SELECT * FROM companies c WHERE c.status = 'INACTIVE'",
    countQuery = "SELECT count(*) FROM companies WHERE status = 'INACTIVE'",
    nativeQuery = true)
    Page<Company> findAllInactiveCompanies(Pageable pageable);

}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\repository\JobRepository.java =====
package com.caochung.recruitment.repository;

import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    boolean existsByName(String name);

    List<Job> findAllBySkillsContaining(Skill skill);

    List<Job> findAllByActive(JobStatusEnum active);

    @Modifying
    @Query("UPDATE Job j SET j.active = 'INACTIVE', j.updatedAt = :now, j.updatedBy = :updatedBy WHERE j.company.id = :companyId")
    void inactivateJobsByCompanyId(Instant now, String updatedBy,Long companyId);

    @Modifying
    @Query(value = "UPDATE jobs SET active = 'CLOSED', updated_at = CURRENT_TIMESTAMP WHERE company_id = :companyId", nativeQuery = true)
    void restoreJobsByCompanyId(Long companyId);

    boolean existsByIdAndCompany_Id(Long jobId, Long id);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\repository\PermissionRepository.java =====
package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission,Long>, JpaSpecificationExecutor<Permission> {
    boolean existsByApiPathAndMethodAndModule(String apiPath, String method, String module);

    boolean existsByApiPathAndMethodAndModuleAndIdNot(String apiPath, String method, String module, Long id);

}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\repository\ResumeRepository.java =====
package com.caochung.recruitment.repository;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume,Long>, JpaSpecificationExecutor<Resume> {
    boolean existsByJob_IdAndStatusIn(Long id, List<ResumeStatusEnum> statusEnums);

    boolean existsByJob_IdAndEmail(Long id,  String email);

    @Modifying
    @Query("UPDATE Resume r SET r.status = 'SYSTEM_CANCEL', r.updatedAt = :now, r.updatedBy = :updatedBy WHERE r.job.company.id = :companyId")
    void inactivateResumeByCompanyId(Instant now, String updatedBy, Long companyId);

    @Modifying
    @Query("UPDATE Resume r SET r.status = 'WITHDRAWN', r.updatedAt = :now, r.updatedBy = :updatedBy WHERE r.user.id = :userId")
    void withdrawnResumeByUserId(Instant now, String updatedBy, Long userId);

    boolean existsByIdAndJob_Company_Id(Long resumeId, Long id);

    boolean existsByIdAndUser_Id(Long resumeId, Long id);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\repository\RoleRepository.java =====
package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.RoleRequestDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long>, JpaSpecificationExecutor<Role> {
    boolean existsByName(String name);

    Role findByName(String name);

    List<Role> findAllByPermissionsContains(Permission permission);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\repository\SkillRepository.java =====
package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long>, JpaSpecificationExecutor<Skill> {
    boolean existsByName(String name);

}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\repository\SubscriberRepository.java =====
package com.caochung.recruitment.repository;

import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.domain.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long>, JpaSpecificationExecutor<Subscriber> {
    boolean existsByEmail(String email);

    List<Subscriber> findAllBySkillsContaining(Skill skill);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\repository\UserRepository.java =====
package com.caochung.recruitment.repository;

import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.domain.User;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User findByRefreshTokenAndEmail(String refreshToken, String email);

    List<User> findAllByCompany(Company company);

    @Modifying
    @Query("UPDATE User u SET u.status = 'DISABLED', u.refreshToken=null, u.updatedAt = :now, u.updatedBy = :updatedBy WHERE u.company.id = :companyId")
    void inactivateUsersByCompanyId(Instant now, String updatedBy, Long companyId);

    @Query(value = "SELECT * FROM users u WHERE u.id = :id", nativeQuery = true)
    Optional<User> findByIdIncludingDeleted(Long id);

    @Query(value = "SELECT * FROM users u WHERE u.status = 'DISABLED'",
            countQuery = "SELECT count(*) FROM users WHERE status = 'DISABLED'",
            nativeQuery = true)
    Page<User> findAllDisableUsers(Pageable pageable);


    @Modifying
    @Query(value = "UPDATE users SET status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    void restoreUserById(Long id);

    @Modifying
    @Query(value = "UPDATE users  SET status = 'ACTIVE', refresh_token=null, updated_at = CURRENT_TIMESTAMP WHERE company_id = :companyId", nativeQuery = true)
    void restoreUsersByCompanyId(Long companyId);

    boolean existsByRole(Role role);

    @Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
    User findByEmailIgnoringSoftDelete(String email);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\scheduler\JobAlertScheduler.java =====
package com.caochung.recruitment.scheduler;

import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.SubscriberRepository;
import com.caochung.recruitment.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "SCHEDULER-JOB")
public class JobAlertScheduler {
    private final JobRepository jobRepository;
    private final EmailService emailService;
    private final SubscriberRepository subscriberRepository;

    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional(readOnly = true)
    public void scheduledJobAlertEmail() {
        log.info("SCHEDULER JOB ALERT EMAIL");
        List<Job> jobs = jobRepository.findAllByActive(JobStatusEnum.OPEN);
        List<Subscriber> subscribers = subscriberRepository.findAll();

        for (Subscriber subscriber : subscribers) {
            List<Job> matchedJobs = jobs.stream()
                    .filter(job -> isSkillMatched(subscriber, job)).toList();
            if (!matchedJobs.isEmpty()) {
                emailService.sendJobAlertEmail(subscriber.getEmail(), subscriber.getName(), matchedJobs);
            }
        }
        log.info("SCHEDULER JOB ALERT EMAIL FINISHED");
    }

    private boolean isSkillMatched(Subscriber subscriber, Job job) {
        return !Collections.disjoint(job.getSkills(), subscriber.getSkills());
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\scheduler\TrendingJobScheduler.java =====
package com.caochung.recruitment.scheduler;

import com.caochung.recruitment.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "SCHEDULER-JOB-TRENDING")
public class TrendingJobScheduler {
    private final RedisTemplate<String, String> redisTemplate;
    @Scheduled(cron = "0 0 0 * * *")
    public void resetTrendingJobScheduler() {
        String key = "trending_jobs";
        Set<ZSetOperations.TypedTuple<String>> jobTrendings= redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
        if(jobTrendings!=null){
            for(ZSetOperations.TypedTuple<String> jobTrending:jobTrendings){
                String jobId = jobTrending.getValue();
                if(jobTrending.getScore()!=null){
                    double newScore=jobTrending.getScore()*0.5;
                    if(newScore<1.0){
                        redisTemplate.opsForZSet().remove(key,jobId);
                    }else {
                        redisTemplate.opsForZSet().add(key,jobId,newScore);
                    }
                }
            }
        }
        log.info("Reset trending job scheduler");
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\AuthService.java =====
package com.caochung.recruitment.service;

import com.caochung.recruitment.dto.request.RegisterDTO;
import com.caochung.recruitment.dto.request.ResetPasswordRequestDTO;
import com.caochung.recruitment.dto.request.VerifyOtpDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    @Transactional(rollbackFor = Exception.class)
    UserResponseDTO registerAndSendOtp(RegisterDTO registerDTO);

    @Transactional
    void VerifyOtp(VerifyOtpDTO verifyOtpDTO);

    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\CloudinaryService.java =====
package com.caochung.recruitment.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CloudinaryService {
    String uploadFile(MultipartFile file);

    List<String> uploadFiles(MultipartFile[] files) throws IOException;
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\CompanyService.java =====
package com.caochung.recruitment.service;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.exception.AppException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CompanyService {
    CompanyResponseDTO createCompany(CompanyRequestDTO requestDTO);

    PaginationResponseDTO getAllCompanies(Specification<Company> specification, Pageable pageable);

    CompanyResponseDTO getCompanyById(Long id);

    void updateCompany(Long id, CompanyRequestDTO requestDTO);

    void deleteCompany(Long id);

    PaginationResponseDTO getAllInactiveCompanies(Pageable pageable);

    @Transactional
    void restoreCompanyById(Long id);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\EmailService.java =====
package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.event.ForgotPasswordEvent;
import com.caochung.recruitment.event.ResumeStatusUpdateEvent;
import com.caochung.recruitment.event.UserRegisterEvent;

import java.util.List;

public interface EmailService {
    void sendJobAlertEmail(String to, String subscriberName, List<Job> jobs);

    void emailVerification(UserRegisterEvent user);

    void sendResumeStatusEmail(ResumeStatusUpdateEvent resume);

    void sendForgotPasswordEmail(ForgotPasswordEvent user);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\JobService.java =====
package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.dto.request.JobRequestDTO;
import com.caochung.recruitment.dto.response.JobResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface JobService {
    JobResponseDTO createJob(JobRequestDTO jobRequestDTO);

    PaginationResponseDTO getJobs(Specification<Job> specification, Pageable pageable, boolean isDashBoard);

    JobResponseDTO getJobById(Long id);

    void updateJob(Long id, JobRequestDTO jobRequestDTO);

    void deleteJob(Long id);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\PermissionService.java =====
package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.dto.request.PermissionRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.PermissionResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PermissionService {
    PermissionResponseDTO createPermission(PermissionRequestDTO permissionRequestDTO);

    PaginationResponseDTO getPermissions(Specification<Permission> specification, Pageable  pageable);

    PermissionResponseDTO getPermissionById(Long id);

    void updatePermission(Long id, PermissionRequestDTO permissionRequestDTO);

    void deletePermission(Long id);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\RedisService.java =====
package com.caochung.recruitment.service;

import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

public interface RedisService {
    void savePasswordOtp(String email, String otp);
    String getPasswordOtp(String email);
    void deletePasswordOtp(String email);

    void saveRegisterData(String email, String otp, String userJson);

    boolean isSpamming(String email);

    String getRegisterOtp(String email);

    String getRegisterData(String email);

    void clearRegistrationData(String email);

    void createBacklistToken(String token, long expiration);

    boolean isExpireToken(String token);

    void countViewJob(Long jobId);

    Set<String> getJobTrendings();

}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\ResumeService.java =====
package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.dto.request.ResumeRequestDTO;
import com.caochung.recruitment.dto.request.ResumeUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResumeResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ResumeService {
    ResumeResponseDTO submitResume(ResumeRequestDTO resumeRequestDTO);

    void updateResume(Long id, ResumeUpdateDTO resumeUpdateDTO);

    void deleteResume(Long id);

    PaginationResponseDTO getResumes(Specification<Resume> specification, Pageable pageable);

    ResumeResponseDTO getResumeById(Long id);

    PaginationResponseDTO getResumeByUser(Pageable pageable);

//    PaginationResponseDTO getResumeByCompany(Specification<Resume> specification ,Pageable pageable);

}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\RoleService.java =====
package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.RoleRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.RoleResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface RoleService {
    RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO);

    PaginationResponseDTO getRoles(Specification<Role> specification, Pageable pageable);

    RoleResponseDTO getRoleById(Long id);

    void updateRole(Long id, RoleRequestDTO roleRequestDTO);

    void deleteRole(Long id);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\SkillService.java =====
package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.dto.request.SkillRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.SkillResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface SkillService {
    SkillResponseDTO createSkill(SkillRequestDTO skillRequestDTO);

    PaginationResponseDTO getSkills(Specification<Skill> specification, Pageable pageable);

    SkillResponseDTO getSkillById(Long id);

    void updateSkill(Long id, SkillRequestDTO skillRequestDTO);

    void deleteSkill(Long id);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\SubscriberService.java =====
package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.dto.request.SubscriberRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.SubscriberResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface SubscriberService {
    SubscriberResponseDTO createSubscriber(SubscriberRequestDTO subscriberRequestDTO);

    PaginationResponseDTO getAllSubscriber(Specification<Subscriber> specification, Pageable pageable);

    SubscriberResponseDTO getSubscriberById(Long id);

    void updateSubscriber(Long id, SubscriberRequestDTO subscriberRequestDTO);

    void deleteSubscriber(Long id);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\UserService.java =====
package com.caochung.recruitment.service;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.RegisterDTO;
import com.caochung.recruitment.dto.request.UserRequestDTO;
import com.caochung.recruitment.dto.request.UserUpdateDTO;
import com.caochung.recruitment.dto.request.VerifyOtpDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.exception.AppException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

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

    PaginationResponseDTO getAllDisableUser(Pageable pageable);

    void restoreUserById(Long id);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\AuthServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.RegisterDTO;
import com.caochung.recruitment.dto.request.ResetPasswordRequestDTO;
import com.caochung.recruitment.dto.request.VerifyOtpDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.event.UserRegisterEvent;
import com.caochung.recruitment.exception.AppException;
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

        // Event send otp to user
        UserRegisterEvent event = UserRegisterEvent.builder()
                .email(registerDTO.getEmail())
                .otpToken(otp)
                .build();
        publisher.publishEvent(event);
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
            UserRegisterEvent event = UserRegisterEvent.builder()
                    .email(user.getEmail())
                    .otpToken(otp)
                    .build();
            publisher.publishEvent(event);
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
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\CloudinaryServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file){
        try{
            if (file.isEmpty()) {
                throw new AppException(ErrorCode.FILE_EMPTY);
            }
            String publicValue = generatePublicValue(file.getOriginalFilename());

            var uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicValue,
                    "resource_type", "auto",
                    "folder", "resume"
            ));
            return String.valueOf(uploadResult.get("secure_url"));
        }catch (Exception e){
            throw new AppException(ErrorCode.FILE_ERROR);
        }

    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file));
        }
        return urls;
    }

    public String generatePublicValue(String originalName){
        String fileName = getFileName(originalName);
        String cleanFileName = fileName.replaceAll("\\s+", "_").toLowerCase();
        return UUID.randomUUID().toString()+"_"+cleanFileName;
    }

    public String getFileName(String originalName){
        if( originalName==null || originalName.isEmpty()){
            return "unknown";
        }
        int index = originalName.lastIndexOf('.');
        if(index==-1){
            return originalName;
        }
        return originalName.substring(0, index);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\CompanyServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.ResumeRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.CompanyService;
import com.caochung.recruitment.service.mapper.CompanyMapper;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;

    @Override
    @Transactional
    public CompanyResponseDTO createCompany(CompanyRequestDTO requestDTO){
        if(companyRepository.existsByName(requestDTO.getName())){
            throw new AppException(ErrorCode.COMPANY_EXISTED);
        }
        Company company = companyMapper.toEntity(requestDTO);
        company.setStatus(CompanyStatusEnum.ACTIVE);
        Company saveCompany = this.companyRepository.save(company);
        return companyMapper.toDto(saveCompany);
    }

    @Override
    public PaginationResponseDTO getAllCompanies(Specification<Company> specification, Pageable pageable) {
        Page<Company> pageCompany = this.companyRepository.findAll(specification, pageable);
        List<Company> companyEntities = pageCompany.getContent();
        List<CompanyResponseDTO> responseDTOs = companyMapper.toDto(companyEntities);

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(pageCompany.getTotalPages())
                .totalItems(pageCompany.getTotalElements())
                .build();

        return new PaginationResponseDTO(meta, responseDTOs);
    }

    @Override
    @Cacheable(value = "company_detail", key = "#id")
    public CompanyResponseDTO getCompanyById(Long id){
        Company company = this.companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        return companyMapper.toDto(company);
    }

    @Override
    @Transactional
    @CacheEvict(value = "company_detail", key = "#id")
    public void updateCompany(Long id, CompanyRequestDTO requestDTO) {
        Company company = this.companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        if(!requestDTO.getName().equals(company.getName())
                && companyRepository.existsByName(requestDTO.getName())){
            throw new AppException(ErrorCode.COMPANY_EXISTED);
        }
        companyMapper.fromUpdateDto(requestDTO, company);
    }

    @Override
    @Transactional
    @CacheEvict(value = "company_detail", key = "#id")
    public void deleteCompany(Long id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        Instant now =  Instant.now();
        String currentUser = SecurityUtil.getCurrentUserLogin().orElse("SYSTEM");
        jobRepository.inactivateJobsByCompanyId(now, currentUser, company.getId());
        resumeRepository.inactivateResumeByCompanyId(now, currentUser, company.getId());
        userRepository.inactivateUsersByCompanyId(now, currentUser, company.getId());
        companyRepository.delete(company);
    }

    @Override
    public PaginationResponseDTO getAllInactiveCompanies(Pageable pageable) {
        Page<Company> pageCompany = this.companyRepository.findAllInactiveCompanies(pageable);
        List<CompanyResponseDTO> responseDTOs = companyMapper.toDto(pageCompany.getContent());

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(pageCompany.getTotalPages())
                .totalItems(pageCompany.getTotalElements())
                .build();

        return new PaginationResponseDTO(meta, responseDTOs);
    }

    @Transactional
    @Override
    public void restoreCompanyById(Long id){
        Company company = companyRepository.findByIdIncludingDeleted(id).orElseThrow(
                () ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        if (company.getStatus().equals(CompanyStatusEnum.ACTIVE)){
            throw new AppException(ErrorCode.COMPANY_ALREADY_ACTIVE);
        }
        jobRepository.restoreJobsByCompanyId(company.getId());
        userRepository.restoreUsersByCompanyId(company.getId());
        companyRepository.restoreCompanyById(company.getId());
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\EmailServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.event.ForgotPasswordEvent;
import com.caochung.recruitment.event.ResumeStatusUpdateEvent;
import com.caochung.recruitment.event.UserRegisterEvent;
import com.caochung.recruitment.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void sendJobAlertEmail(String to, String subscriberName, List<Job> jobs) {
        try{
            log.info("SEND JOB ALERT EMAIL TO {}", to);
            Context context = new Context();
            context.setVariable("name", subscriberName);
            context.setVariable("jobs", jobs);
            String emailContent = templateEngine.process("job-alert", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject("Có "+ jobs.size()+ " việc làm mới phù hợp với bạn hôm nay!");
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
            log.info("Email send successful");
        } catch (MessagingException e) {
            log.error("ERROR SEND EMAIL TO {} : {}", to, e.getMessage());
        }
    }

    @Override
    public void emailVerification(UserRegisterEvent user) {
        try{
            log.info("SEND EMAIL VERIFICATION TO {}", user.getEmail());
            Context context = new Context();
            context.setVariable("otpCode", user.getOtpToken());
            String emailContent = templateEngine.process("email-verification", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(user.getEmail());
            helper.setSubject("Account verification");
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("ERROR SEND EMAIL TO {} : {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    public void sendResumeStatusEmail(ResumeStatusUpdateEvent resume){
        try{
            log.info("SEND RESUME STATUS EMAIL TO {}", resume.getEmailTo());
            Context context = new Context();
            context.setVariable("resume", resume);
            String emailContent = templateEngine.process("resume-status-update", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(resume.getEmailTo());
            helper.setSubject("Resume status update");
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
            log.info("EMAIL SEND SUCCESSFUL");
        } catch (MessagingException e) {
            log.error("ERROR SEND EMAIL TO {} : {}", resume.getEmailTo(), e.getMessage());
        }
    }

    @Override
    public void sendForgotPasswordEmail(ForgotPasswordEvent user){
        try{
            log.info("SEND FORGOT PASSWORD EMAIL TO {}", user.getEmail());
            Context context = new Context();
            context.setVariable("otpCode", user.getOtpToken());
            String emailContent = templateEngine.process("forgot-password", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(user.getEmail());
            helper.setSubject("Forgot Password");
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        }catch (MessagingException e) {
            log.error("ERROR SEND EMAIL TO {} : {}", user.getEmail(), e.getMessage());
        }
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\JobServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.JobRequestDTO;
import com.caochung.recruitment.dto.response.JobResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.ResumeRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.JobService;
import com.caochung.recruitment.service.RedisService;
import com.caochung.recruitment.service.mapper.JobMapper;
import com.caochung.recruitment.util.SecurityUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public JobResponseDTO createJob(JobRequestDTO jobRequestDTO) {
        if(this.jobRepository.existsByName(jobRequestDTO.getName())){
            throw new AppException(ErrorCode.JOB_EXISTED);
        }
        Job job = this.jobMapper.toJob(jobRequestDTO);
        return this.jobMapper.toDto(this.jobRepository.save(job));
    }

    @Override
    public PaginationResponseDTO getJobs(Specification<Job> specification, Pageable pageable, boolean isDashBoard) {
        if (isDashBoard) {
            String email = SecurityUtil.getCurrentUserLogin().isPresent()
                    ? SecurityUtil.getCurrentUserLogin().get() : null;
            User user = this.userRepository.findByEmail(email).orElse(null);
            if (user.getCompany() != null) {
                Specification<Job> spec = (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("company"), user.getCompany());
                specification = specification.and(spec);
            }
        }
        Page<Job> jobs = this.jobRepository.findAll(specification, pageable);
        List<JobResponseDTO> jobResponseDTOS = this.jobMapper.toDto(jobs.getContent());
        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(jobs.getTotalPages())
                .totalItems(jobs.getTotalElements())
                .build();
        return new PaginationResponseDTO(meta, jobResponseDTOS);
    }

    @Override
    @Cacheable(value = "job_detail", key = "#id")
    public JobResponseDTO getJobById(Long id) {
        Job job = this.jobRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        return this.jobMapper.toDto(job);
    }

    @Override
    @Transactional
    @CacheEvict(value = "job_detail", key = "#id")
    public void updateJob(Long id, JobRequestDTO jobRequestDTO) {
        Job job = this.jobRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        if(!job.getName().equals(jobRequestDTO.getName()) && this.jobRepository.existsByName(jobRequestDTO.getName())){
            throw new AppException(ErrorCode.JOB_EXISTED);
        }
        this.jobMapper.fromUpdateJob(jobRequestDTO, job);
    }

    @Override
    @Transactional
    @CacheEvict(value = "job_detail", key = "#id")
    public void deleteJob(Long id) {
        Job job = this.jobRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        if(this.resumeRepository.existsByJob_IdAndStatusIn(id, List.of(ResumeStatusEnum.PENDING, ResumeStatusEnum.REVIEWING))){
            throw new AppException(ErrorCode.JOB_HAS_ACTIVE_RESUMES);
        }
        this.jobRepository.delete(job);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\PermissionServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.PermissionRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.PermissionResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.PermissionRepository;
import com.caochung.recruitment.repository.RoleRepository;
import com.caochung.recruitment.service.PermissionService;
import com.caochung.recruitment.service.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {
    private final PermissionMapper permissionMapper;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public PermissionResponseDTO createPermission(PermissionRequestDTO permissionRequestDTO) {
        checkPermission(permissionRequestDTO);
        Permission permission = this.permissionMapper.toPermission(permissionRequestDTO);
        Permission savedPermission = this.permissionRepository.save(permission);
        return permissionMapper.toDTO(savedPermission);
    }

    @Override
    public PaginationResponseDTO getPermissions(Specification<Permission> specification, Pageable pageable) {
        Page<Permission> permissionPage = this.permissionRepository.findAll(specification, pageable);
        List<PermissionResponseDTO> permissionResponseDTOS = this.permissionMapper.toDTO(permissionPage.getContent());

        PaginationResponseDTO.Meta meta= PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(permissionPage.getTotalPages())
                .totalItems(permissionPage.getTotalElements())
                .build();
        return new PaginationResponseDTO(meta, permissionResponseDTOS);
    }

    @Override
    public PermissionResponseDTO getPermissionById(Long id) {
        Permission permission = this.permissionRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.PERMISSION_NOT_FOUND)
        );
        return permissionMapper.toDTO(permission);
    }

    @Override
    @Transactional
    public void updatePermission(Long id, PermissionRequestDTO permissionRequestDTO) {
        Permission permission = this.permissionRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        if( permissionRepository.existsByApiPathAndMethodAndModuleAndIdNot(permissionRequestDTO.getApiPath(),
                permissionRequestDTO.getMethod(), permissionRequestDTO.getModule(), id)){
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }
        this.permissionMapper.fromUpdate(permissionRequestDTO,permission);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = this.permissionRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        List<Role> roles = this.roleRepository.findAllByPermissionsContains(permission);
        if(!roles.isEmpty()){
            throw new AppException(ErrorCode.PERMISSION_HAS_USED);
        }
        this.permissionRepository.deleteById(id);
    }

    private void checkPermission(PermissionRequestDTO permissionRequestDTO) {
        if( permissionRepository.existsByApiPathAndMethodAndModule(permissionRequestDTO.getApiPath(),
                permissionRequestDTO.getMethod(), permissionRequestDTO.getModule())){
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\RedisServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "OPT-SERVICE")
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String FORGOT_PASSWORD_PREFIX = "forgot_password";
    private static final String REGISTER_DATA_PREFIX = "register_data";
    private static final String REGISTER_OTP_PREFIX = "register_otp";
    private static final String REGISTER_COOLDOWN_PREFIX = "register_cooldown";
    private static final String BACKLIST_TOKEN_PREFIX = "backlist_token";
    private static final String TRENDING_JOBS = "trending_jobs";
    @Override
    public void savePasswordOtp(String email, String otp) {
        String key = FORGOT_PASSWORD_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(5));
        log.info("Saved OTP into redis for email: {}", email);
    }

    @Override
    public String getPasswordOtp(String email) {
        String key = FORGOT_PASSWORD_PREFIX + email;
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deletePasswordOtp(String email) {
        String key = FORGOT_PASSWORD_PREFIX + email;
        redisTemplate.delete(key);
        log.info("Deleted OTP into redis for email: {}", email);
    }

    @Override
    public void saveRegisterData(String email, String otp, String userJson){
        redisTemplate.opsForValue().set(REGISTER_DATA_PREFIX + email, userJson, Duration.ofMinutes(5));
        redisTemplate.opsForValue().set(REGISTER_OTP_PREFIX + email, otp, Duration.ofMinutes(5));
        redisTemplate.opsForValue().set(REGISTER_COOLDOWN_PREFIX + email, "wait", Duration.ofMinutes(60));
        log.info("Saved register data into redis for email: {}", email);
    }

    @Override
    public boolean isSpamming(String email){
        return redisTemplate.hasKey(REGISTER_COOLDOWN_PREFIX + email);
    }

    @Override
    public String getRegisterOtp(String email){
        return redisTemplate.opsForValue().get(REGISTER_OTP_PREFIX + email);
    }

    @Override
    public String getRegisterData(String email){
        return redisTemplate.opsForValue().get(REGISTER_DATA_PREFIX + email);
    }

    @Override
    public void clearRegistrationData(String email) {
        redisTemplate.delete(REGISTER_OTP_PREFIX + email);
        redisTemplate.delete(REGISTER_DATA_PREFIX + email);
        log.info("Clear registration data into redis for email: {}", email);
    }

    @Override
    public void createBacklistToken(String token, long expiration){
        redisTemplate.opsForValue().set(BACKLIST_TOKEN_PREFIX+token, "revoked", Duration.ofSeconds(expiration));
    }

    @Override
    public boolean isExpireToken(String token){
        return redisTemplate.hasKey(BACKLIST_TOKEN_PREFIX + token);
    }

    @Override
    public void countViewJob(Long jobId){
        redisTemplate.opsForZSet().incrementScore(TRENDING_JOBS, jobId.toString(), 1);
    }

    @Override
    public Set<String> getJobTrendings(){
        return redisTemplate.opsForZSet().reverseRange(TRENDING_JOBS, 0, 9);
    }

}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\ResumeServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.ResumeRequestDTO;
import com.caochung.recruitment.dto.request.ResumeUpdateDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResumeResponseDTO;
import com.caochung.recruitment.event.ResumeStatusUpdateEvent;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.ResumeRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.CloudinaryService;
import com.caochung.recruitment.service.ResumeService;
import com.caochung.recruitment.service.mapper.ResumeMapper;
import com.caochung.recruitment.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.EventListener;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional
    public ResumeResponseDTO submitResume(ResumeRequestDTO resumeRequestDTO) {
        Job job = jobRepository.findById(resumeRequestDTO.getJobId()).orElseThrow(
                () -> new AppException(ErrorCode.JOB_NOT_FOUND));
        if(job.getActive().equals(JobStatusEnum.CLOSED) || job.getActive().equals(JobStatusEnum.DRAFT)){
            throw new AppException(ErrorCode.JOB_INACTIVE);
        }
        if (job.getCompany().getStatus().equals(CompanyStatusEnum.INACTIVE)) {
            throw new AppException(ErrorCode.COMPANY_INACTIVE);
        }
        if(resumeRepository.existsByJob_IdAndEmail(resumeRequestDTO.getJobId(), resumeRequestDTO.getEmail())){
            throw new AppException(ErrorCode.ALREADY_APPLIED);
        }
        Resume resume = this.resumeMapper.toResume(resumeRequestDTO);
        return resumeMapper.toDTO(this.resumeRepository.save(resume));
    }

    @Override
    @Transactional
    public void updateResume(Long id, ResumeUpdateDTO resumeUpdateDTO) {
        Resume resume = resumeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        resumeMapper.fromUpdate(resumeUpdateDTO, resume);
        ResumeStatusUpdateEvent event = ResumeStatusUpdateEvent.builder()
                .emailTo(resume.getEmail())
                .username(resume.getUser().getName())
                .jobName(resume.getJob().getName())
                .companyName(resume.getJob().getCompany().getName())
                .status(resume.getStatus())
                .build();
        publisher.publishEvent(event);
    }

    @Override
    @Transactional
    public void deleteResume(Long id) {
        Resume resume = resumeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(()->new AppException(ErrorCode.UNAUTHENTICATED));
        ResumeStatusUpdateEvent event = ResumeStatusUpdateEvent.builder()
                .emailTo(resume.getEmail())
                .username(resume.getUser().getName())
                .jobName(resume.getJob().getName())
                .companyName(resume.getJob().getCompany().getName())
                .build();
        if(resume.getEmail().equals(email)){
            resume.setStatus(ResumeStatusEnum.WITHDRAWN);
            event.setStatus(ResumeStatusEnum.WITHDRAWN);
        }else {
            resume.setStatus(ResumeStatusEnum.SYSTEM_CANCEL);
            event.setStatus(ResumeStatusEnum.SYSTEM_CANCEL);
        }
        publisher.publishEvent(event);
    }

    @Override
    public ResumeResponseDTO getResumeById(Long id) {
        Resume resume = resumeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        return resumeMapper.toDTO(resume);
    }

    @Override
    public PaginationResponseDTO getResumeByUser(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHENTICATED));

        Specification<Resume> spec = (root, query, cb) ->
                cb.equal(root.get("user").get("email"), email);
        Page<Resume> resumePage = this.resumeRepository.findAll(spec, pageable);
        List<ResumeResponseDTO>  resumeResponseDTOS = this.resumeMapper.toDTO(resumePage.getContent());
        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalItems(resumePage.getTotalElements())
                .totalPages(resumePage.getTotalPages())
                .build();
        return new PaginationResponseDTO(meta, resumeResponseDTOS);
    }

    @Override
    public PaginationResponseDTO getResumes(Specification<Resume> specification, Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(()->new AppException(ErrorCode.UNAUTHENTICATED));

        User user = this.userRepository.findByEmail(email).orElseThrow(()->
                new AppException(ErrorCode.USER_NOT_FOUND));

        Specification<Resume> finalSpec = specification;
        if (user.getCompany() != null){
            Specification<Resume> spec = (root, query, cb) ->
                    cb.equal(root.get("job").get("company").get("id"), user.getCompany().getId());
            finalSpec = (specification == null) ? spec : specification.and(spec);
        }

        Page<Resume> resumePage = this.resumeRepository.findAll(finalSpec, pageable);
        List<ResumeResponseDTO> resumeResponseDTOS = this.resumeMapper.toDTO(resumePage.getContent());

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalItems(resumePage.getTotalElements())
                .totalPages(resumePage.getTotalPages())
                .build();
        return new PaginationResponseDTO(meta, resumeResponseDTOS);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\RoleServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.RoleRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.RoleResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.RoleRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.RoleService;
import com.caochung.recruitment.service.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO) {
        if(roleRepository.existsByName(roleRequestDTO.getName())){
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        Role role = roleMapper.toRole(roleRequestDTO);
        Role savedRole = roleRepository.save(role);
        return roleMapper.toDTO(savedRole);
    }

    @Override
    public PaginationResponseDTO getRoles(Specification<Role> specification, Pageable pageable) {
        Page<Role> rolePage = roleRepository.findAll(specification, pageable);
        List<RoleResponseDTO> roleResponseDTOS = roleMapper.toDTO(rolePage.getContent());

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(rolePage.getTotalPages())
                .totalItems(rolePage.getTotalElements())
                .build();
        return new PaginationResponseDTO(meta, roleResponseDTOS);
    }

    @Override
    public RoleResponseDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        return roleMapper.toDTO(role);
    }

    @Override
    @Transactional
    public void updateRole(Long id, RoleRequestDTO roleRequestDTO) {
        Role role = roleRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        if(!role.getName().equals(roleRequestDTO.getName()) && roleRepository.existsByName(roleRequestDTO.getName())){
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        this.roleMapper.fromUpdate(roleRequestDTO, role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = this.roleRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        if(userRepository.existsByRole(role)){
            throw new AppException(ErrorCode.ROLE_ALREADY_ACTIVE);
        }
        this.roleRepository.deleteById(id);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\SkillServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.dto.request.SkillRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.SkillResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.SkillRepository;
import com.caochung.recruitment.repository.SubscriberRepository;
import com.caochung.recruitment.service.SkillService;
import com.caochung.recruitment.service.mapper.SkillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillServiceImpl implements SkillService {
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final SubscriberRepository subscriberRepository;
    private final SkillMapper skillMapper;

    @Override
    @Transactional
    public SkillResponseDTO createSkill(SkillRequestDTO skillRequestDTO) {
        if(skillRepository.existsByName(skillRequestDTO.getName())){
            throw new AppException(ErrorCode.SKILL_EXISTED);
        }
        Skill skill = skillMapper.toSkill(skillRequestDTO);
        Skill savedSkill = skillRepository.save(skill);
        return skillMapper.toDto(savedSkill);
    }

    @Override
    public PaginationResponseDTO getSkills(Specification<Skill> specification, Pageable pageable) {
        Page<Skill> skillPage = this.skillRepository.findAll(specification, pageable);
        List<SkillResponseDTO> skillResponseDTOS = this.skillMapper.toDto(skillPage.getContent());
        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(skillPage.getTotalPages())
                .totalItems(skillPage.getTotalElements())
                .build();
        return new PaginationResponseDTO(meta, skillResponseDTOS);
    }

    @Override
    @Cacheable(value = "skill_detail", key = "#id")
    public SkillResponseDTO getSkillById(Long id) {
        Skill skill = this.skillRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_FOUND));
        return skillMapper.toDto(skill);
    }

    @Override
    @Transactional
    @CacheEvict(value = "skill_detail", key = "#id")
    public void updateSkill(Long id, SkillRequestDTO skillRequestDTO) {
        Skill skill = this.skillRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.SKILL_NOT_FOUND));
        if(!skill.getName().equals(skillRequestDTO.getName()) && skillRepository.existsByName(skillRequestDTO.getName())){
            throw new AppException(ErrorCode.SKILL_EXISTED);
        }
        skill.setName(skillRequestDTO.getName());
    }

    @Override
    @Transactional
    @CacheEvict(value = "skill_detail", key = "#id")
    public void deleteSkill(Long id) {
        Skill skill = this.skillRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.SKILL_NOT_FOUND));

        List<Job> jobs = this.jobRepository.findAllBySkillsContaining(skill);
        if (!jobs.isEmpty()) {
            throw new AppException(ErrorCode.SKILL_HAS_USED);
        }

        List<Subscriber> subscribers = this.subscriberRepository.findAllBySkillsContaining(skill);
        if (!subscribers.isEmpty()) {
            throw new AppException(ErrorCode.SKILL_HAS_USED);
        }
        this.skillRepository.delete(skill);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\SubscriberServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.dto.request.SubscriberRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.SubscriberResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.SubscriberRepository;
import com.caochung.recruitment.service.SubscriberService;
import com.caochung.recruitment.service.mapper.SubscriberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriberServiceImpl implements SubscriberService {
    private final SubscriberRepository subscriberRepository;
    private final SubscriberMapper subscriberMapper;

    @Override
    @Transactional
    public SubscriberResponseDTO createSubscriber(SubscriberRequestDTO subscriberRequestDTO) {
        if(subscriberRepository.existsByEmail(subscriberRequestDTO.getEmail())){
            throw new AppException(ErrorCode.SUBSCRIBER_EXISTED);
        }
        Subscriber subscriber = subscriberMapper.toEntity(subscriberRequestDTO);
        Subscriber savedSubscriber = this.subscriberRepository.save(subscriber);
        return subscriberMapper.toDTO(savedSubscriber);
    }

    @Override
    public PaginationResponseDTO getAllSubscriber(Specification<Subscriber> specification, Pageable pageable) {
        Page<Subscriber> pageSubscribers = this.subscriberRepository.findAll(specification, pageable);
        List<SubscriberResponseDTO> responseDTOs = subscriberMapper.toDTO(pageSubscribers.getContent());

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(pageSubscribers.getTotalPages())
                .totalItems(pageSubscribers.getTotalElements())
                .build();

        return new PaginationResponseDTO(meta, responseDTOs);
    }

    @Override
    public SubscriberResponseDTO getSubscriberById(Long id) {
        Subscriber subscriber = this.subscriberRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.SUBSCRIBER_NOT_FOUND));
        return subscriberMapper.toDTO(subscriber);
    }

    @Override
    @Transactional
    public void updateSubscriber(Long id, SubscriberRequestDTO subscriberRequestDTO) {
        Subscriber subscriber = this.subscriberRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.SUBSCRIBER_NOT_FOUND));
        if(!subscriber.getEmail().equals(subscriberRequestDTO.getEmail())
                && subscriberRepository.existsByEmail(subscriberRequestDTO.getEmail())){
            throw new AppException(ErrorCode.SUBSCRIBER_EXISTED);
        }
        subscriberMapper.fromUpdate(subscriberRequestDTO, subscriber);
    }

    @Override
    @Transactional
    public void deleteSubscriber(Long id) {
        this.subscriberRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.SUBSCRIBER_NOT_FOUND));
        this.subscriberRepository.deleteById(id);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\impl\UserServiceImpl.java =====
package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.domain.User;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ResumeRepository resumeRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * create user by admin
     * @param userRequestDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * Update info user
     * @param id
     * @param userUpdateDTO
     */
    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.fromUpdate(userUpdateDTO, user);
    }

    /**
     * Delete user by id
     * @param id
     */
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
        return this.userRepository.findByEmail(username).orElseThrow(
                ()-> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toDto(user);
    }

    /**
     * update refresh token
     * @param token
     * @param email
     */
    @Override
    @Transactional
    public void updateUserToken(String token, String email) {
        this.userRepository.findByEmail(email).ifPresent(user -> user.setRefreshToken(token));
    }

    @Override
    public User getUserByRefreshTokenAndEmail(String refreshToken,  String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }

    /**
     * Get all user has been soft deleted
     * @param pageable
     * @return
     */
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

    /**
     * Restore user
     * @param id
     */
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
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\mapper\CompanyMapper.java =====
package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    Company toEntity(CompanyRequestDTO companyRequestDTO);

    CompanyResponseDTO toDto(Company company);

    List<CompanyResponseDTO> toDto(List<Company> companyList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void fromUpdateDto(CompanyRequestDTO companyRequestDTO, @MappingTarget Company company);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\mapper\JobMapper.java =====
package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.dto.request.JobRequestDTO;
import com.caochung.recruitment.dto.request.SkillRequestDTO;
import com.caochung.recruitment.dto.response.JobResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.SkillRepository;
import com.caochung.recruitment.service.JobService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class JobMapper {
    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Mapping(source = "companyId", target = "company")
    public abstract Job toJob(JobRequestDTO jobRequestDTO);

    public abstract JobResponseDTO toDto(Job job);

    public abstract List<JobResponseDTO> toDto(List<Job> jobs);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract Job fromUpdateJob(JobRequestDTO jobRequestDTO,@MappingTarget Job job);

    public abstract JobResponseDTO.JobCompany toDtoCompany(Company company);

    protected List<Skill> mapSkills(List<Long> skills) {
        if (skills == null || skills.isEmpty()) {
            return null;
        }
        return this.skillRepository.findAllById(skills);
    }

    protected Company mapCompany(Long id){
        if (id == null) {
            return null;
        }else {
            return companyRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.COMPANY_NOT_FOUND));
        }
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\mapper\PermissionMapper.java =====
package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.dto.request.PermissionRequestDTO;
import com.caochung.recruitment.dto.response.PermissionResponseDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequestDTO permissionRequestDTO);

    PermissionResponseDTO toDTO(Permission permission);

    List<PermissionResponseDTO> toDTO(List<Permission> permissions);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void fromUpdate(PermissionRequestDTO permissionRequestDTO,@MappingTarget Permission permission);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\mapper\ResumeMapper.java =====
package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.ResumeRequestDTO;
import com.caochung.recruitment.dto.request.ResumeUpdateDTO;
import com.caochung.recruitment.dto.response.ResumeResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ResumeMapper {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Mapping(source = "userId", target = "user")
    @Mapping(source = "jobId", target = "job")
    public abstract Resume toResume(ResumeRequestDTO resumeRequestDTO);

    @Mapping(source = "job.company.name", target = "companyName")
    public abstract ResumeResponseDTO toDTO(Resume resume);
    public abstract ResumeResponseDTO.UserResume toDTO(User user);
    public abstract ResumeResponseDTO.JobResume toDTO(Job job);

    public abstract void fromUpdate(ResumeUpdateDTO resumeUpdateDTO,@MappingTarget Resume resume);

    public abstract List<ResumeResponseDTO> toDTO(List<Resume> resumes);

    protected User mapUser(Long id){
        if(id==null){
            return null;
        }
        return userRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    protected Job mapJob(Long id){
        if(id==null){
            return null;
        }
        return jobRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.JOB_NOT_FOUND));
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\mapper\RoleMapper.java =====
package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.dto.request.RoleRequestDTO;
import com.caochung.recruitment.dto.response.LoginResponseDTO;
import com.caochung.recruitment.dto.response.RoleResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.PermissionRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class RoleMapper {
    @Autowired
    private PermissionRepository permissionRepository;

    public abstract Role toRole(RoleRequestDTO roleRequestDTO);

    public abstract RoleResponseDTO toDTO(Role role);

    public abstract RoleResponseDTO.PermissionRole toDTO(Permission permission);

    public abstract List<RoleResponseDTO> toDTO(List<Role> roles);

    public abstract void fromUpdate(RoleRequestDTO roleRequestDTO,@MappingTarget Role role);

    protected Set<Permission> mapRole(List<Long> permissionIds){
        if(permissionIds == null || permissionIds.isEmpty()){
            return null;
        }
        return new HashSet<>(permissionRepository.findAllById(permissionIds));
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\mapper\SkillMapper.java =====
package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.dto.request.SkillRequestDTO;
import com.caochung.recruitment.dto.response.SkillResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    Skill toSkill(SkillRequestDTO skillRequestDTO);
    SkillResponseDTO toDto(Skill skill);
    List<SkillResponseDTO> toDto(List<Skill> skillList);
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\mapper\SubscriberMapper.java =====
package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.domain.Skill;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.dto.request.SubscriberRequestDTO;
import com.caochung.recruitment.dto.response.SubscriberResponseDTO;
import com.caochung.recruitment.repository.SkillRepository;
import com.caochung.recruitment.repository.SubscriberRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class SubscriberMapper {
    @Autowired
    private SkillRepository skillRepository;

    public abstract Subscriber toEntity(SubscriberRequestDTO subscriberRequestDTO);

    public abstract SubscriberResponseDTO toDTO(Subscriber subscriber);

    public abstract List<SubscriberResponseDTO> toDTO(List<Subscriber> subscribers);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void fromUpdate(SubscriberRequestDTO subscriberRequestDTO, @MappingTarget Subscriber subscriber);

    protected List<Skill> mapSkills(List<Long> skills) {
        if (skills == null || skills.isEmpty()) {
            return null;
        }
        return this.skillRepository.findAllById(skills);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\service\mapper\UserMapper.java =====
package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.RegisterDTO;
import com.caochung.recruitment.dto.request.UserRequestDTO;
import com.caochung.recruitment.dto.request.UserUpdateDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.RoleRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Mapping(source = "companyId", target = "company")
    public abstract User toEntity(UserRequestDTO userRequestDTO);

    public abstract UserResponseDTO toDto(User user);

    public abstract List<UserResponseDTO> toDto(List<User> userList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "companyId", target = "company")
    public abstract void fromUpdate(UserUpdateDTO userUpdateDTO, @MappingTarget User user);

    public abstract UserResponseDTO.CompanyResponseDTO toDtoUser(Company company);

    public abstract User toEntity(RegisterDTO registerDTO);

    protected Company mapCompany(Long id){
        if(id==null){
            return null;
        }
        return companyRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.COMPANY_NOT_FOUND));
    }

    protected Role mapRole(Long id){
        if(id==null){
            return null;
        }
        return roleRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.ROLE_NOT_FOUND));
    }

    @AfterMapping
    protected void mapCompany(UserUpdateDTO userUpdateDTO, @MappingTarget User user) {
        if (userUpdateDTO.getCompanyId() != null) {
            Company company = new Company();
            company.setId(userUpdateDTO.getCompanyId());
            user.setCompany(company);
        } else {
            // Nếu companyId gửi lên là null, ta chủ động set null cho user
            user.setCompany(null);
        }
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\CompanyStatusValidator.java =====
package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.util.annotation.CompanyStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompanyStatusValidator implements ConstraintValidator<CompanyStatus, String> {
    private List<String> companyStatus;

    @Override
    public void initialize(CompanyStatus constraintAnnotation) {
        var subset = constraintAnnotation.anyOf();

        if(subset.length == 0){
            companyStatus = Arrays.stream(CompanyStatusEnum.values())
                    .map(Enum::name).toList();
        }
        else{
            companyStatus = Arrays.stream(subset)
                    .map(Enum::name).toList();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null || companyStatus.contains(value);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\GenderValidator.java =====
package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.GenderEnum;
import com.caochung.recruitment.constant.LevelEnum;
import com.caochung.recruitment.util.annotation.Gender;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class GenderValidator implements ConstraintValidator<Gender, String> {

    private List<String> genders;

    @Override
    public void initialize(Gender constraintAnnotation) {
        var subset = constraintAnnotation.anyOf();

        if (subset.length == 0) {
            this.genders = Arrays.stream(GenderEnum.values())
                    .map(Enum::name)
                    .toList();
        } else {
            this.genders = Arrays.stream(subset)
                    .map(Enum::name)
                    .toList();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || genders.contains(value);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\JobStatusValidator.java =====
package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.constant.LevelEnum;
import com.caochung.recruitment.util.annotation.JobStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JobStatusValidator implements ConstraintValidator<JobStatus, String> {
    private List<String> jobStatus;

    @Override
    public void initialize(JobStatus constraintAnnotation) {
        var subset = constraintAnnotation.anyOf();

        if (subset.length == 0) {
            this.jobStatus = Arrays.stream(JobStatusEnum.values())
                    .map(Enum::name)
                    .toList();
        } else {
            this.jobStatus = Arrays.stream(subset)
                    .map(Enum::name)
                    .toList();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && jobStatus.contains(value);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\LevelValidator.java =====
package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.LevelEnum;
import com.caochung.recruitment.util.annotation.Level;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class LevelValidator implements ConstraintValidator<Level,String> {
    private List<String> levels;

    @Override
    public void initialize(Level constraintAnnotation) {
        var subset = constraintAnnotation.anyOf();

        if (subset.length == 0) {
            this.levels = Arrays.stream(LevelEnum.values())
                    .map(Enum::name)
                    .toList();
        } else {
            this.levels = Arrays.stream(subset)
                    .map(Enum::name)
                    .toList();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && levels.contains(value);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\OtpGenerator.java =====
package com.caochung.recruitment.util;

import java.security.SecureRandom;

public class OtpGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\ResumeStatusValidator.java =====
package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.util.annotation.ResumeStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResumeStatusValidator implements ConstraintValidator<ResumeStatus, String> {
    private List<String> resumeStatus;

    @Override
    public void initialize(ResumeStatus constraintAnnotation) {
        var subset = constraintAnnotation.anyOf();
        if(subset.length == 0){
            this.resumeStatus = Arrays.stream(ResumeStatusEnum.values())
                    .map(Enum::name).toList();
        }
        else{
            this.resumeStatus = Arrays.stream(subset)
                    .map(Enum::name).toList();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && resumeStatus.contains(value);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\SecurityUtil.java =====
package com.caochung.recruitment.util;

import com.caochung.recruitment.config.CustomUserDetails;
import com.caochung.recruitment.dto.response.LoginResponseDTO;
import com.caochung.recruitment.dto.response.RoleResponseDTO;
import com.nimbusds.jose.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
@RequiredArgsConstructor
public class SecurityUtil {

    private final JwtEncoder jwtEncoder;

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    @Value("${caochung.jwt.base64-secret}")
    private String jwtKey;

    @Value("${caochung.jwt.access-token-validity-in-second}")
    private long accessTokenExpiration;

    @Value("${caochung.jwt.refresh-token-validity-in-second}")
    private long refreshTokenExpiration;

    public String createAccessToken(Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        List<String> authorities = customUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        LoginResponseDTO.UserInsideToken userToken = LoginResponseDTO.UserInsideToken.builder()
                .id(customUserDetails.getId())
                .email(customUserDetails.getEmail())
                .username(customUserDetails.getName())
                .build();

        Instant now = Instant.now();
        Instant expiration = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(customUserDetails.getEmail())
                .claim("user", userToken)
                .claim("permission", authorities)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String createRefreshToken(Authentication  authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        LoginResponseDTO.UserInsideToken userToken = LoginResponseDTO.UserInsideToken.builder()
                .id(customUserDetails.getId())
                .email(customUserDetails.getEmail())
                .username(customUserDetails.getName())
                .build();

        Instant now = Instant.now();
        Instant expiration = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(customUserDetails.getEmail())
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
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\UserStatusValidator.java =====
package com.caochung.recruitment.util;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.util.annotation.CompanyStatus;
import com.caochung.recruitment.util.annotation.UserStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class UserStatusValidator implements ConstraintValidator<UserStatus, String> {
    private List<String> userStatus;

    @Override
    public void initialize(UserStatus constraintAnnotation) {
        var subset = constraintAnnotation.anyOf();

        if(subset.length == 0){
            userStatus = Arrays.stream(UserStatusEnum.values())
                    .map(Enum::name).toList();
        }
        else{
            userStatus = Arrays.stream(subset)
                    .map(Enum::name).toList();
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null || userStatus.contains(value);
    }
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\annotation\CompanyStatus.java =====
package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.util.CompanyStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = CompanyStatusValidator.class)
public @interface CompanyStatus {
    CompanyStatusEnum[] anyOf() default {};
    String message() default "Invalid Company Status, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\annotation\Gender.java =====
package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.GenderEnum;
import com.caochung.recruitment.util.GenderValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenderValidator.class)
public @interface Gender {
    GenderEnum[] anyOf();
    String message() default "Invalid Gender, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\annotation\JobStatus.java =====
package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.util.JobStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = JobStatusValidator.class)
public @interface JobStatus {
    JobStatusEnum[] anyOf() default {};
    String message() default "Invalid Job Status, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\annotation\Level.java =====
package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.LevelEnum;
import com.caochung.recruitment.util.LevelValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = LevelValidator.class)
public @interface Level {
    LevelEnum[] anyOf() default {};
    String message() default "Invalid Level, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\annotation\ResumeStatus.java =====
package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.util.ResumeStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = ResumeStatusValidator.class)
public @interface ResumeStatus {
    ResumeStatusEnum[] anyOf() default {};
    String message() default "Invalid Resume Status, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
// ===== File: C:\Users\Acer\Documents\My_Workspace\recruitment\src\main\java\com\caochung\recruitment\util\annotation\UserStatus.java =====
package com.caochung.recruitment.util.annotation;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.util.CompanyStatusValidator;
import com.caochung.recruitment.util.UserStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = UserStatusValidator.class)
public @interface UserStatus {
    UserStatusEnum[] anyOf() default {};
    String message() default "Invalid User Status, must {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
