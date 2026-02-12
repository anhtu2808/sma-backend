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
    CAN_NOT_CLOSED(HttpStatus.BAD_REQUEST, "Current job can not be closed, please check job status"),
    CAN_NOT_DRAFTED(HttpStatus.BAD_REQUEST, "Current job can not be drafted, please check job status"),
    INVALID_JOB_STATUS(HttpStatus.BAD_REQUEST, "Job status is invalid"),
    CANT_DELETE_RESUME_IN_USE(HttpStatus.BAD_REQUEST, "Cannot delete resume that is in use"),
    EMAIL_EXISTS(HttpStatus.BAD_REQUEST, "Email already exists"),
    INVALID_SEPAY_CONTENT_FORMAT(HttpStatus.BAD_REQUEST, "Invalid sepay content format"),

    //401 - Unauthenticated
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Unauthenticated"),
    PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, "Password incorrect"),
    ACCOUNT_INACTIVE(HttpStatus.UNAUTHORIZED, "Your account is inactive"),

    //403 - Unauthorized
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "Unauthorized"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "Refresh token expired"),
    NOT_HAVE_PERMISSION(HttpStatus.FORBIDDEN, "You do not have permission to access this resource"),
    CAN_NOT_CHANGE_DIRECT_TO_PENDING(HttpStatus.FORBIDDEN, "You do not have permission to change job status to pending review directly"),
    PAYMENT_TIME_EXPIRED(HttpStatus.FORBIDDEN, "Payment time expired"),
    PLAN_UPDATE_ONLY_PRICE_ALLOWED(HttpStatus.FORBIDDEN, "Only price updates are allowed when subscriptions exist"),
    CANDIDATE_CAN_NOT_LOGIN(HttpStatus.FORBIDDEN, "Candidate can not be logged in"),

    //404 - Not found
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    USER_NOT_EXISTED(HttpStatus.NOT_FOUND, "User does not exist"),
    ROLE_NOT_EXISTED(HttpStatus.NOT_FOUND, "Role does not exist"),
    EMAIL_NOT_EXISTED(HttpStatus.NOT_FOUND, "Email does not exist"),
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
    DOMAIN_ALREADY_EXISTED(HttpStatus.NOT_FOUND, "Domain already existed"),
    DOMAIN_NOT_FOUND(HttpStatus.NOT_FOUND, "Domain not found"),
    CANT_DELETE_DOMAIN_IN_USE(HttpStatus.NOT_FOUND, "Cannot delete domain that is in use"),
    CATEGORY_ALREADY_EXITED(HttpStatus.NOT_FOUND, "Category already existed"),
    CANT_DELETE_CATEGORY_IN_USE(HttpStatus.NOT_FOUND, "Cannot delete category that is in use"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Category not found"),
    SKILL_ALREADY_EXITED(HttpStatus.NOT_FOUND, "Skill already existed"),
    CANT_DELETE_SKILL_IN_USE(HttpStatus.NOT_FOUND, "Cannot delete skill that is in use"),
    GROUP_ALREADY_EXITED(HttpStatus.NOT_FOUND, "Expertise group already existed"),
    EXPERTISE_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "Expertise group not found"),
    CANT_DELETE_EXPERTISE_IN_USE(HttpStatus.NOT_FOUND, "Cannot delete expertise that is in use"),
    EXPERTISE_ALREADY_EXITED(HttpStatus.NOT_FOUND, "Expertise already existed"),
    EXPERTISE_GROUP_IN_USE(HttpStatus.NOT_FOUND, "Cannot delete expertise group that is in use"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    CRITERIA_NOT_EXISTED(HttpStatus.NOT_FOUND, "Criteria does not exist"),
    JOB_QUESTION_NOT_EXIST(HttpStatus.NOT_FOUND, "Job question does not exist"),
    ALREADY_REJECTED_FOR_THIS_JOB(HttpStatus.NOT_FOUND, "You have already been rejected for this job"),
    MAX_APPLY_ATTEMPTS_REACHED (HttpStatus.NOT_FOUND, "You have reached the maximum number of application attempts for this job"),
    CANNOT_REAPPLY_AFTER_PROCESSING (HttpStatus.NOT_FOUND, "You cannot reapply for this job after your previous application has been processed"),
    REQUIRED_QUESTION_NOT_ANSWERED (HttpStatus.NOT_FOUND, "You must answer all required questions for this job"),
    RESUME_PARSE_FAILED(HttpStatus.NOT_FOUND, "Your resume parsing failed, please upload a new resume to apply"),
    RESUME_STILL_PARSING(HttpStatus.NOT_FOUND, "Your resume is still being parsed, please wait until the parsing is complete to apply"),
    RESUME_ALREADY_DELETED(HttpStatus.NOT_FOUND, "This resume has already been deleted"),
    PACKAGE_NOT_EXIST(HttpStatus.NOT_FOUND, "Package does not exist"),
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "Plan not found"),
    FEATURE_NOT_FOUND(HttpStatus.NOT_FOUND, "Feature not found"),
    PLAN_ALREADY_EXISTS(HttpStatus.NOT_FOUND, "Plan already exists"),
    PLAN_PRICE_NOT_FOUND(HttpStatus.NOT_FOUND, "Plan price not found"),
    FEATURE_KEY_EXISTS(HttpStatus.BAD_REQUEST, "Feature key already exists"),
    FEATURE_NAME_EXISTS(HttpStatus.BAD_REQUEST, "Feature name already exists"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Payment not found"),
    FEATURE_DISABLED(HttpStatus.FORBIDDEN, "Feature is disabled"),
    FEATURE_NOT_INCLUDED(HttpStatus.FORBIDDEN, "Feature is not included in active subscription"),
    FEATURE_QUOTA_EXCEEDED(HttpStatus.FORBIDDEN, "Feature quota exceeded"),
    INVALID_FEATURE_USAGE_AMOUNT(HttpStatus.BAD_REQUEST, "Invalid feature usage amount"),
    STATE_CHECKER_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "State checker not configured"),

    //500 - Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    String message;
    HttpStatusCode statusCode;

    ErrorCode(HttpStatusCode statusCode, String message) {
        this.message = message;
        this.statusCode = statusCode;
    }

}
