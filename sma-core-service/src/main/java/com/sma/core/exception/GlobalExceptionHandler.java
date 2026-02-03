package com.sma.core.exception;

import com.sma.core.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e) {

        HttpStatusCode code = ErrorCode.BAD_REQUEST.getStatusCode();

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity
                .status(code)
                .body(ApiResponse.<Map<String, String>>builder()
                        .code(code.value())
                        .message("Validation failed")
                        .data(errors)
                        .build());
    }

    /**
     * Error validating param/path
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException e) {

        HttpStatusCode code = ErrorCode.BAD_REQUEST.getStatusCode();

        String message = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Invalid request parameters");

        return ResponseEntity
                .status(code)
                .body(ApiResponse.<Void>builder()
                        .code(code.value())
                        .message(message)
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

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {

        HttpStatusCode code = HttpStatus.NOT_FOUND;

        return ResponseEntity
                .status(code)
                .body(ApiResponse.<Void>builder()
                        .code(code.value())
                        .message("API endpoint not found")
                        .build());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException e) {

        HttpStatusCode code = HttpStatus.NOT_FOUND;

        return ResponseEntity
                .status(code)
                .body(ApiResponse.<Void>builder()
                        .code(code.value())
                        .message("API endpoint not found")
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
