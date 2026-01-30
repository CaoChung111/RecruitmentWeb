package com.caochung.recruitment.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessCode {
    GET_SUCCESS(HttpStatus.OK, "Get Success"),
    CREATED_SUCCESS(HttpStatus.CREATED,"Created success"),
    PUT_SUCCESS(HttpStatus.OK,"Put Success"),
    DELETE_SUCCESS(HttpStatus.OK,"Delete Success"),

    LOGIN_SUCCESS(HttpStatus.CREATED, "Login Success"),
    LOGOUT_SUCCESS(HttpStatus.CREATED, "Logout Success"),;

    private final HttpStatus status;
    private final String message;

    SuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
