package com.sma.core.service.quota.impl;

import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.enums.UserStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.service.quota.StateQuotaChecker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeamMemberLimitStateChecker implements StateQuotaChecker {

    RecruiterRepository recruiterRepository;

    @Override
    public long getCurrentUsage(QuotaOwnerContext ownerContext, Object input) {
        if (ownerContext.getCompanyId() == null) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }
        return recruiterRepository.countByCompanyIdAndUser_Status(ownerContext.getCompanyId(), UserStatus.ACTIVE);
    }
}
