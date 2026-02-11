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
