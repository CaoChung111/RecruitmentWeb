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
