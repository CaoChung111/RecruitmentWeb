package com.caochung.recruitment.messaging.dto;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Jacksonized
public class EmailNotificationMessage implements Serializable {
    // Phục vụ Idempotency: tránh gửi trùng mail nếu xảy ra network retry
    @Builder.Default
    private String messageId = UUID.randomUUID().toString();

    // Phục vụ Audit và kiểm tra tin nhắn quá hạn
    @Builder.Default
    private Instant createdAt = Instant.now();

    // Thông tin cần để gửi Email
    private String emailTo;
    private String otpToken;
    private String username;
    private String jobName;
    private String companyName;
    private ResumeStatusEnum status;

    // Định tuyến loại email cần gửi
    private NotificationType notificationType;
}
