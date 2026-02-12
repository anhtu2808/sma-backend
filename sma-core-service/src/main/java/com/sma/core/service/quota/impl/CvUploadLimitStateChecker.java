package com.sma.core.service.quota.impl;

import com.sma.core.enums.ResumeType;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.service.quota.QuotaOwnerContext;
import com.sma.core.service.quota.StateQuotaChecker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CvUploadLimitStateChecker implements StateQuotaChecker {

    ResumeRepository resumeRepository;

    @Override
    public long getCurrentUsage(QuotaOwnerContext ownerContext, Object input) {
        if (ownerContext.getCandidateId() == null) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }
        return resumeRepository.countActiveByCandidateIdAndType(ownerContext.getCandidateId(), ResumeType.ORIGINAL);
    }
}
