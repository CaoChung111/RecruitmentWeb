package com.caochung.recruitment.domain.dto.response;

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
