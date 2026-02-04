package com.sma.core.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {

    //400 - Bad request
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),
    USER_EXISTS(HttpStatus.BAD_REQUEST, "User already exists"),
    JOB_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "Job not available"),

    //401 - Unauthenticated
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Unauthenticated"),
    PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, "Password incorrect"),

    //403 - Unauthorized
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "Unauthorized"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "Refresh token expired"),

    //404 - Not found
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    USER_NOT_EXISTED(HttpStatus.NOT_FOUND, "User does not exist"),
    ROLE_NOT_EXISTED(HttpStatus.NOT_FOUND, "Role does not exist"),
    EMAIL_NOT_EXISTED(HttpStatus.NOT_FOUND, "Email does not exist"),
    EMAIL_EXISTS(HttpStatus.NOT_FOUND, "Email already exists"),
    COMPANY_ALREADY_REGISTERED(HttpStatus.NOT_FOUND, "Company already registered"),
    TOKEN_NOT_EXISTED(HttpStatus.NOT_FOUND, "Token does not exist"),
    JOB_NOT_EXISTED(HttpStatus.NOT_FOUND, "Job does not exist"),

    //500 - Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    String message;
    HttpStatusCode statusCode;

    ErrorCode(HttpStatusCode statusCode, String message) {
        this.message = message;
        this.statusCode = statusCode;
    }

}
