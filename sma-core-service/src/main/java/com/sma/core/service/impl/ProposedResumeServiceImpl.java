package com.sma.core.service.impl;

import com.sma.core.dto.message.embedding.job.EmbeddingJobRequestMessage;
import com.sma.core.dto.message.proposed.ProposedCVRequestMessage;
import com.sma.core.dto.message.proposed.ProposedCVResultMessage;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.ProposedCVResponse;
import com.sma.core.entity.Job;
import com.sma.core.entity.ProposedResume;
import com.sma.core.entity.Resume;
import com.sma.core.enums.EmbedStatus;
import com.sma.core.enums.ProposeStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.ProposedResumeMapper;
import com.sma.core.mapper.job.JobMapper;
import com.sma.core.messaging.embedding.job.EmbeddingJobRequestPublisher;
import com.sma.core.messaging.proposed.ProposedCVRequestPublisher;
import com.sma.core.repository.JobRepository;
import com.sma.core.repository.ProposedResumeRepository;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.service.ProposedResumeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProposedResumeServiceImpl implements ProposedResumeService {

    ResumeRepository resumeRepository;
    JobRepository jobRepository;
    ProposedResumeRepository proposedResumeRepository;
    ProposedResumeMapper proposedResumeMapper;
    EmbeddingJobRequestPublisher embeddingJobRequestPublisher;
    JobMapper jobMapper;
    ProposedCVRequestPublisher proposedCVRequestPublisher;

    @Override
    public void addProposedResume(ProposedCVResultMessage message) {
        Job job = jobRepository.findById(message.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        if (message.getStatus().equals(ProposeStatus.FAILED)) {
            job.setProposeStatus(ProposeStatus.FAILED);
            jobRepository.save(job);
            log.info("Error message: {}", message.getErrorMessage());
            throw new AppException(ErrorCode.SERVER_ERROR_PROPOSE);
        }
        job.setProposeStatus(message.getStatus());
        jobRepository.save(job);
        List<ProposedResume> proposedResumes = new ArrayList<>();
        message.getProposedCVs()
                .forEach(proposedCVData -> {
                    Resume resume = resumeRepository.findById(proposedCVData.getResumeId())
                            .orElse(null);
                    if (resume != null) {
                        if (!proposedResumeRepository.existsByJobIdAndResumeId(job.getId(), resume.getId())) {
                            ProposedResume proposedResume = ProposedResume.builder()
                                    .job(job)
                                    .resume(resume)
                                    .matchRate(proposedCVData.getMatchRate())
                                    .build();
                            proposedResumes.add(proposedResume);
                        }
                    }
                });

        proposedResumeRepository.saveAll(proposedResumes);
    }

    @Override
    public PagingResponse<ProposedCVResponse> getProposedCV(Integer jobId, Integer page, Integer size) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        boolean isNotEmbedYet = false;
        if (!job.getEmbedStatus().equals(EmbedStatus.SUCCESS)) {
            embeddingJobRequestPublisher.publish(jobMapper.toEmbeddingJobMessage(job));
        }
        if (!job.getProposeStatus().equals(ProposeStatus.FINISHED)) {
            job.setProposeStatus(ProposeStatus.PROCESSING);
            isNotEmbedYet = true;
            proposedCVRequestPublisher.publish(ProposedCVRequestMessage.builder()
                    .id(jobId)
                    .build());
        }
        if (isNotEmbedYet)
            jobRepository.save(job);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "matchRate"));
        Page<ProposedResume> proposedResumes = proposedResumeRepository.findByJobId(jobId, pageable);

        List<ProposedCVResponse> proposedCVResponses = proposedResumes.getContent()
                .stream().map(proposedResumeMapper::toProposeCVResponse).toList();
        return PagingResponse.fromPage(proposedResumes, proposedCVResponses);
    }
}
