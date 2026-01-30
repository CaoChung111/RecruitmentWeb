package com.caochung.recruitment.config;

import com.caochung.recruitment.util.SecurityUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfiuration {
    @Bean
    public AuditorAware<String> auditorProvider(){
        return SecurityUtil::getCurrentUserLogin;
    }
}
