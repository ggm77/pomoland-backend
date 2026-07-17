package com.seohamin.pomoland.global.exception.handler;

import com.seohamin.pomoland.global.exception.CustomException;
import com.seohamin.pomoland.global.exception.response.ExceptionResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
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

    // Long.parseLong(userIdStr) 등 문자열을 숫자로 파싱하다 실패했을 때 처리용
    // (예: GET /api/v1/users/abc). 처리 안 하면 500으로 떨어짐
    @ExceptionHandler(NumberFormatException.class)
    public final ResponseEntity<ExceptionResponse> handleNumberFormatException(final NumberFormatException ex) {

        final ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "요청 정보가 잘못되어 있습니다."
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    // 엔티티의 Bean Validation(@NotNull, @Size 등)이 트랜잭션 도중 명시적 flush(saveAndFlush 등)로
    // 즉시 터졌을 때 처리용. DTO에 별도 검증이 없어 서비스단 수동 검사를 통과한 값이
    // 엔티티 제약에 걸리는 경우가 있음
    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<ExceptionResponse> handleConstraintViolationException(final ConstraintViolationException ex) {

        final ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "요청 정보가 잘못되어 있습니다."
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    // 위와 같은 Bean Validation 위반이 명시적 flush 없이 트랜잭션 커밋 시점에 터지면
    // Spring이 이를 TransactionSystemException으로 감싸서 던짐 (원인은 RollbackException -> ConstraintViolationException)
    // 예: 유저명 100자 초과로 저장 시도
    @ExceptionHandler(TransactionSystemException.class)
    public final ResponseEntity<ExceptionResponse> handleTransactionSystemException(final TransactionSystemException ex) {

        final Throwable rootCause = ex.getMostSpecificCause();
        if (!(rootCause instanceof ConstraintViolationException)) {
            log.error("CustomizedResponseEntityExceptionHandler.handleTransactionSystemException message:{}", ex.getMessage(), ex);
            final ExceptionResponse serverErrorResponse = new ExceptionResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "INTERNAL_SERVER_ERROR",
                    "Internal Server Error"
            );
            return new ResponseEntity<>(serverErrorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        final ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "요청 정보가 잘못되어 있습니다."
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
