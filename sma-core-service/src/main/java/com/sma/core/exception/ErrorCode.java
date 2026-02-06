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
    COMPANY_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "Company not available"),
    INVALID_TOTAL_WEIGHT(HttpStatus.BAD_REQUEST, "Total weight must be equal 100"),
    CAN_NOT_PUBLISH(HttpStatus.BAD_REQUEST, "Current job can not be published, please check job status"),

    //401 - Unauthenticated
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Unauthenticated"),
    PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, "Password incorrect"),
    ACCOUNT_INACTIVE(HttpStatus.UNAUTHORIZED, "Your account is inactive"),

    //403 - Unauthorized
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "Unauthorized"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "Refresh token expired"),
    NOT_HAVE_PERMISSION(HttpStatus.FORBIDDEN, "You do not have permission to access this resource"),

    //404 - Not found
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    USER_NOT_EXISTED(HttpStatus.NOT_FOUND, "User does not exist"),
    ROLE_NOT_EXISTED(HttpStatus.NOT_FOUND, "Role does not exist"),
    EMAIL_NOT_EXISTED(HttpStatus.NOT_FOUND, "Email does not exist"),
    EMAIL_EXISTS(HttpStatus.NOT_FOUND, "Email already exists"),
    COMPANY_ALREADY_REGISTERED(HttpStatus.NOT_FOUND, "Company already registered"),
    TOKEN_NOT_EXISTED(HttpStatus.NOT_FOUND, "Token does not exist"),
    STATUS_ALREADY_FINALIZED(HttpStatus.NOT_FOUND, "Company status already finalized"),
    MUST_BE_UNDER_REVIEW_FIRST(HttpStatus.NOT_FOUND, "Company must be under review first"),
    INVALID_STATUS_TRANSITION(HttpStatus.NOT_FOUND, "Invalid status transition"),
    JOB_NOT_EXISTED(HttpStatus.NOT_FOUND, "Job does not exist"),
    COMPANY_NOT_EXISTED(HttpStatus.NOT_FOUND, "Company does not exist"),
    RESUME_NOT_EXISTED(HttpStatus.NOT_FOUND, "Resume does not exist"),
    CANDIDATE_NOT_EXISTED(HttpStatus.NOT_FOUND, "Candidate does not exist"),
    RECRUITER_NOT_EXISTED(HttpStatus.NOT_FOUND, "Recruiter does not exist"),
    CRITERIA_NOT_EXISTED(HttpStatus.NOT_FOUND, "Criteria does not exist"),

    //500 - Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    String message;
    HttpStatusCode statusCode;

    ErrorCode(HttpStatusCode statusCode, String message) {
        this.message = message;
        this.statusCode = statusCode;
    }

}
