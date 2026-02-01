package com.sma.core.exception;

import com.sma.core.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle all business errors.
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getStatusCode().value())
                        .message(errorCode.getMessage())
                        .build());
    }

    /**
     * Validation error @Valid body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e) {
            HttpStatusCode code = ErrorCode.BAD_REQUEST.getStatusCode();
        return ResponseEntity
                .status(code)
                .body(ApiResponse.<Void>builder()
                        .code(code.value())
                        .message("Invalid request data")
                        .build());
    }

    /**
     * Error validating param/path
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException e) {
        HttpStatusCode code = ErrorCode.BAD_REQUEST.getStatusCode();
        return ResponseEntity
                .status(code)
                .body(ApiResponse.<Void>builder()
                        .code(code.value())
                        .message("Invalid request parameters")
                        .build());
    }

    /**
     * Spring Security – access denied
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException e) {
        HttpStatusCode code = ErrorCode.UNAUTHORIZED.getStatusCode();
        return ResponseEntity
                .status(code)
                .body(ApiResponse.<Void>builder()
                        .code(code.value())
                        .message(ErrorCode.UNAUTHORIZED.getMessage())
                        .build());
    }

    /**
     * Fallback – unexpected error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception e) {
        log.error("Unhandled exception", e);
        HttpStatusCode code = ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode();
        return ResponseEntity
                .status(code)
                .body(ApiResponse.<Void>builder()
                        .code(code.value())
                        .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                        .build());
    }
}
