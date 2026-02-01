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

    //401 - Unauthenticated
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Unauthenticated"),

    //403 - Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),

    //404 - Not found
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),

    //500 - Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    String message;
    HttpStatusCode statusCode;

    ErrorCode(HttpStatusCode statusCode, String message) {
        this.message = message;
        this.statusCode = statusCode;
    }

}
