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

    USER_EXISTED("User already existed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),
    EMAIL_EXISTED("Email already existed", HttpStatus.BAD_REQUEST),

    FILE_ERROR("File error", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_TOO_LARGE("File too long", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_EMPTY("File is empty", HttpStatus.BAD_REQUEST),

    COMPANY_EXISTED("Company already existed", HttpStatus.BAD_REQUEST),
    COMPANY_NOT_FOUND("Company not found", HttpStatus.NOT_FOUND),

    SKILL_EXISTED("Skill already existed", HttpStatus.BAD_REQUEST),
    SKILL_NOT_FOUND("Skill not found", HttpStatus.NOT_FOUND),

    JOB_EXISTED("Job already existed", HttpStatus.BAD_REQUEST),
    JOB_NOT_FOUND("Job not found", HttpStatus.NOT_FOUND),

    RESUME_NOT_FOUND("Resume not found",  HttpStatus.NOT_FOUND),

    PERMISSION_NOT_FOUND("Permission not found",  HttpStatus.NOT_FOUND),
    PERMISSION_EXISTED("Permission already existed",  HttpStatus.BAD_REQUEST),

    ROLE_EXISTED("Role already existed",  HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND("Role not found",  HttpStatus.NOT_FOUND),;

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
