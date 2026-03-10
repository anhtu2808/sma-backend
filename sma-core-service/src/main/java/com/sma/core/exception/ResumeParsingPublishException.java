package com.sma.core.exception;

public class ResumeParsingPublishException extends AppException {

    public ResumeParsingPublishException(Throwable cause) {
        super(ErrorCode.INTERNAL_SERVER_ERROR);
        initCause(cause);
    }
}
