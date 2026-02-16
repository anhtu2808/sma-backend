package com.sma.core.service;

import com.sma.core.entity.Job;

public interface BannedKeywordService {
    boolean isContentViolated(Job job);
}
