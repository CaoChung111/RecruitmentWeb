package com.caochung.recruitment.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION("Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY("Invalid message key", HttpStatus.BAD_REQUEST),
    INVALID_ACCESS_TOKEN("Invalid access token", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("Invalid refresh token", HttpStatus.UNAUTHORIZED),
    VALIDATION_ERROR("Validation failed", HttpStatus.BAD_REQUEST),

    USER_EXISTED("User already existed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),
    EMAIL_EXISTED("Email already existed", HttpStatus.BAD_REQUEST),

    COMPANY_EXISTED("Company already existed", HttpStatus.BAD_REQUEST),
    COMPANY_NOT_FOUND("Company not found", HttpStatus.NOT_FOUND);


    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
