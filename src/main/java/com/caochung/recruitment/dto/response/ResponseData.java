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
