package com.sma.core.service;

import com.sma.core.dto.message.resume.ResumeParsingResultMessage;

public interface ResumeParsingResultService {
    void processParsingResult(ResumeParsingResultMessage message);
}
