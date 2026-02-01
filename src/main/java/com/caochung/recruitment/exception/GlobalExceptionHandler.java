package com.caochung.recruitment.exception;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.dto.response.ResponseError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;


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
