package com.seohamin.pomoland.global.exception.handler;

import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.response.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    // 커스텀으로 만든 예외들 처리
    @ExceptionHandler(CustomException.class)
    public final ResponseEntity<ExceptionResponse> handleException(final CustomException ex) {

        final String message;
        if(ex.getMessage() == null || ex.getMessage().isEmpty()) {
            message = ex.getExceptionCode().getMessage();
        } else {
            message = ex.getMessage();
        }

        final ExceptionResponse exceptionResponse = new ExceptionResponse(
                ex.getExceptionCode().getStatus(),
                ex.getExceptionCode().name(),
                message
        );

        return new ResponseEntity<>(exceptionResponse, ex.getExceptionCode().getStatus());
    }

    // NotNull 어노테이션이 발생 시키는 예외 처리용
    // ResponseEntityExceptionHandler에 이미 존재해서 override함
    @Override
    public final ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request
    ) {

        final ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "요청 정보가 완전하지 않습니다."
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    // 나머지 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> handleAllException(final Exception ex) {
        final ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Internal Server Error"
        );

        log.error("CustomizedResponseEntityExceptionHandler.handleAllExceptions message:{}", ex.getMessage(), ex);

        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
