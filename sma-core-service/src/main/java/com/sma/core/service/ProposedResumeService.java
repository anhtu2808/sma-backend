package com.sma.core.service;

import com.sma.core.dto.message.proposed.ProposedCVResultMessage;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.ProposedCVResponse;
import com.sma.core.entity.ProposedResume;

public interface ProposedResumeService {

    void addProposedResume(ProposedCVResultMessage message);
    PagingResponse<ProposedCVResponse> getProposedCV(Integer id, Integer page, Integer size);


}
