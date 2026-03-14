package com.sma.core.service.impl;

import com.sma.core.client.AIServiceClient;
import com.sma.core.dto.model.UsageContextModel;
import com.sma.core.dto.message.matching.CriteriaScoreData;
import com.sma.core.dto.message.matching.CriteriaScoreDetailData;
import com.sma.core.dto.message.matching.MatchingRequestMessage;
import com.sma.core.dto.message.matching.MatchingResultData;
import com.sma.core.dto.message.matching.MatchingResultMessage;
import com.sma.core.dto.message.suggest.ReSuggestRequestMessage;
import com.sma.core.dto.message.suggest.SuggestResultMessage;
import com.sma.core.dto.message.suggest.SuggestionRequestMessage;
import com.sma.core.dto.request.evaluation.ManualScoreMatchingRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.evaluation.ResumeEvaluationDetailResponse;
import com.sma.core.dto.response.evaluation.ResumeEvaluationOverviewResponse;
import com.sma.core.dto.response.evaluation.SuggestionResponse;
import com.sma.core.dto.response.suggestion.GapSuggestionResponse;
import com.sma.core.dto.response.suggestion.WeaknessSuggestionResponse;
import com.sma.core.entity.*;
import com.sma.core.enums.*;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.exception.MatchingPublishException;
import com.sma.core.mapper.evaluation.EvaluationMapper;
import com.sma.core.mapper.evaluation.EvaluationResponseMapper;
import com.sma.core.mapper.evaluation.MatchingRequestMapper;
import com.sma.core.mapper.evaluation.MatchingResultMapper;
import com.sma.core.messaging.matching.MatchingRequestPublisher;
import com.sma.core.messaging.suggest.SuggestionRequestPublisher;
import com.sma.core.repository.*;
import com.sma.core.service.QuotaService;
import com.sma.core.service.ResumeEvaluationService;
import com.sma.core.service.ResumeService;
import com.sma.core.service.ScoringCriteriaService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ResumeEvaluationServiceImpl implements ResumeEvaluationService {

    ResumeEvaluationRepository resumeEvaluationRepository;
    EvaluationWeaknessRepository evaluationWeaknessRepository;
    EvaluationGapRepository evaluationGapRepository;
    EvaluationCriteriaScoreRepository evaluationCriteriaScoreRepository;
    JobRepository jobRepository;
    ResumeRepository resumeRepository;
    ResumeService resumeService;
    MatchingRequestPublisher matchingRequestPublisher;
    MatchingRequestMapper matchingRequestMapper;
    MatchingResultMapper matchingResultMapper;
    EvaluationResponseMapper evaluationResponseMapper;
    QuotaService quotaService;
    SuggestionRequestPublisher suggestionRequestPublisher;
    EvaluationMapper evaluationMapper;
    ScoringCriteriaRepository scoringCriteriaRepository;
    ScoringCriteriaService scoringCriteriaService;
    EvaluationCriteriaSuggestionRepository evaluationCriteriaSuggestionRepository;
    AIServiceClient aiClient;

    @Override
    @Transactional(noRollbackFor = MatchingPublishException.class)
    public Integer processMatching(Integer jobId, Integer resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        validateForMatching(job, resume);
        // Check if an overview evaluation already exists for this (job, resume) pair
        Optional<ResumeEvaluation> existingOverview = resumeEvaluationRepository
                .findByResumeIdAndJobId(resumeId, jobId);

        if (existingOverview.isPresent()) {
            // Supplement mode: reuse overview record, only request gaps/explanations/skills
            ResumeEvaluation evaluation = existingOverview.get();
            evaluation.setEvaluationType(EvaluationType.DETAIL);
            evaluation.setEvaluationStatus(EvaluationStatus.WAITING);
            resumeEvaluationRepository.save(evaluation);

            publishMatchingRequest(
                    evaluation,
                    resume,
                    job,
                    EvaluationType.DETAIL,
                    buildOverviewScoresMap(evaluation)
            );

            log.info("Detail supplement request sent for evaluationId={}, jobId={}, resumeId={}",
                    evaluation.getId(), jobId, resumeId);
            return evaluation.getId();
        } else {
            // Full detail mode: no overview exists, create new record and do full matching
            return doProcessMatching(jobId, resumeId, EvaluationType.DETAIL, resume, job);
        }
    }

    @Override
    @Transactional(noRollbackFor = MatchingPublishException.class)
    public Integer processMatchingOverview(Integer jobId, Integer resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        validateForMatching(job, resume);
        if (job.getEnableAiScoring()) {
            Optional<ResumeEvaluation> existingOverview = resumeEvaluationRepository
                    .findByResumeIdAndJobIdAndEvaluationType(resumeId, jobId, EvaluationType.DETAIL);
            if (existingOverview.isPresent() && existingOverview.get().getEvaluationStatus() == EvaluationStatus.FINISH) {
                return existingOverview.get().getId();
            }
            return doProcessMatching(jobId, resumeId, EvaluationType.OVERVIEW, resume, job);
        }
        return null;
    }

    @Override
    public String getMatchingStatus(Integer id) {
        ResumeEvaluation evaluation = resumeEvaluationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_NOT_EXISTED));
        return evaluation.getEvaluationStatus().name();
    }

    @Override
    public ResumeEvaluationDetailResponse getEvaluationDetail(Integer id) {
        ResumeEvaluation evaluation = resumeEvaluationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_NOT_EXISTED));
        return evaluationResponseMapper.toDetailResponse(evaluation);
    }

    @Override
    public PagingResponse<ResumeEvaluationOverviewResponse> getEvaluationsByJob(Integer jobId, Pageable pageable) {
        Page<ResumeEvaluation> page = resumeEvaluationRepository.findByJobId(jobId, pageable);
        return PagingResponse.fromPage(page.map(evaluationResponseMapper::toOverviewResponse));
    }

    @Override
    public PagingResponse<ResumeEvaluationOverviewResponse> getAllEvaluations(Pageable pageable) {
        Page<ResumeEvaluation> page = resumeEvaluationRepository.findAll(pageable);
        return PagingResponse.fromPage(page.map(evaluationResponseMapper::toOverviewResponse));
    }

    @Override
    @Transactional
    public void processMatchingResult(MatchingResultMessage message) {
        log.info("Processing matching result for evaluationId={}, status={}",
                message.getEvaluationId(), message.getStatus());

        ResumeEvaluation evaluation = resumeEvaluationRepository.findById(message.getEvaluationId())
                .orElseThrow(() -> {
                    log.error("Evaluation not found for id={}", message.getEvaluationId());
                    quotaService.markUsageEventFailed(message.getUsageEventId());
                    return new AppException(ErrorCode.EVALUATION_NOT_EXISTED);
                });

        if (message.getStatus() == EvaluationStatus.FAIL) {
            markMatchingFailed(evaluation, message.getUsageEventId(), message.getErrorMessage());
            return;
        }

        try {
            MatchingResultData data = message.getParsedData();
            if (data == null) {
                markMatchingFailed(
                        evaluation,
                        message.getUsageEventId(),
                        "INVALID_RESULT_MESSAGE: missing parsedData for FINISH status"
                );
                return;
            }

        if (evaluation.getEvaluationType() == EvaluationType.DETAIL
                && hasExistingCriteriaScores(evaluation)) {
            // Detail supplement mode: add explanations, details, and suggestions
            processDetailSupplementResult(data, evaluation);
        } else {
            // Overview or full detail mode: save everything from scratch
            processFullResult(data, evaluation);
        }

            log.info("Successfully processed matching result for evaluationId={}, overallScore={}",
                    evaluation.getId(), evaluation.getAiOverallScore());

        } catch (Exception e) {
            log.error("Error processing matching result for evaluationId={}", message.getEvaluationId(), e);
            markMatchingFailed(evaluation, message.getUsageEventId(), e.getMessage());
        }
    }

    @Override
    public void generateSuggestion(Integer id) {
        ResumeEvaluation evaluation = resumeEvaluationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_NOT_EXISTED));
        SuggestionRequestMessage message = evaluationMapper.toSuggestionRequestMessage(evaluation);
        suggestionRequestPublisher.publish(message);
        log.info("generateSuggestion called for evaluationId={} - not yet implemented", id);
    }

    @Override
    public SuggestionResponse reGenerateSuggestion(Integer suggestionId) {
        EvaluationCriteriaSuggestion suggestion = evaluationCriteriaSuggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUGGESTION_NOT_FOUND));
        ReSuggestRequestMessage message = evaluationMapper.toReSuggestRequestMessage(suggestion);
        SuggestResultMessage resultMessage = aiClient.reSuggestion(message);
        if (resultMessage == null || resultMessage.getErrorMessage() != null || !resultMessage.getStatus().equals("SUCCESS")) {
            throw new AppException(ErrorCode.SERVER_ERROR_RE_SUGGESTION);
        }
        suggestion.setSuggestion(resultMessage.getSuggestion());
        evaluationCriteriaSuggestionRepository.save(suggestion);
        return SuggestionResponse.builder()
                .id(suggestion.getId())
                .suggestion(suggestion.getSuggestion())
                .build();
    }

    @Transactional
    @Override
    public void saveSuggestion(SuggestResultMessage message) {
    }

    @Override
    @Transactional
    public ResumeEvaluationDetailResponse scoreManual(Integer id, ManualScoreMatchingRequest request) {
        ResumeEvaluation evaluation = resumeEvaluationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_NOT_EXISTED));

        evaluation.setRecruiterOverallScore(request.getManualScore());

        // Update manual score for each criteria
        if (request.getScoreCriteriaRequests() != null) {
            for (var criteriaRequest : request.getScoreCriteriaRequests()) {
                EvaluationCriteriaScore criteriaScore = evaluationCriteriaScoreRepository
                        .findById(criteriaRequest.getEvaluationCriteriaScoreId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_SCORE_NOT_EXISTED));
                criteriaScore.setManualScore(criteriaRequest.getManualScore());
                evaluationCriteriaScoreRepository.save(criteriaScore);
            }
        }

        resumeEvaluationRepository.save(evaluation);
        return evaluationResponseMapper.toDetailResponse(evaluation);
    }

    // ---- Private helpers ----

    private void validateForMatching(Job job, Resume resume) {
        if (!job.getStatus().equals(JobStatus.PUBLISHED)) {
            throw new AppException(ErrorCode.JOB_NOT_AVAILABLE);
        }
        if (!scoringCriteriaRepository.existsByJobId(job.getId())){
            scoringCriteriaService.generateAndSetCriteriaContext(job);
        }
        if (!resume.getParseStatus().equals(ResumeParseStatus.FINISH)) {
            resumeService.parseResume(resume.getId());
        }
    }

    private Integer doProcessMatching(Integer jobId, Integer resumeId, EvaluationType type,
                                      Resume resume, Job job) {
        // Create evaluation with WAITING status
        ResumeEvaluation evaluation = ResumeEvaluation.builder()
                .resume(resume)
                .job(job)
                .evaluationStatus(EvaluationStatus.WAITING)
                .evaluationType(type)
                .build();
        resumeEvaluationRepository.save(evaluation);

        publishMatchingRequest(evaluation, resume, job, type, null);

        log.info("Matching request sent for evaluationId={}, jobId={}, resumeId={}, matchingType={}",
                evaluation.getId(), jobId, resumeId, type);

        return evaluation.getId();
    }

    /**
     * Process full matching result — used for overview and full detail (no prior overview).
     * Saves top-level fields, criteria scores, details, and suggestions.
     */
    private void processFullResult(MatchingResultData data, ResumeEvaluation evaluation) {
        // Map top-level fields (includes transferabilityToRole, isTrueLevel, hasRelatedExperience)
        matchingResultMapper.mapToEvaluation(data, evaluation);
        resumeEvaluationRepository.save(evaluation);

        // Save criteria scores with details and suggestions
        saveCriteriaScores(data.getCriteriaScores(), evaluation);
    }

    /**
     * Process detail supplement result — used when overview already exists.
     * Adds aiExplanation, details, and suggestions to existing record.
     * Does NOT re-score criteria.
     */
    private void processDetailSupplementResult(MatchingResultData data, ResumeEvaluation evaluation) {
        // Update supplementary top-level fields
        if (data.getIsTrueLevel() != null) {
            evaluation.setIsTrueLevel(data.getIsTrueLevel());
        }
        if (data.getHasRelatedExperience() != null) {
            evaluation.setHasRelatedExperience(data.getHasRelatedExperience());
        }
        if (data.getTransferabilityToRole() != null) {
            evaluation.setTransferabilityToRole(data.getTransferabilityToRole());
        }
        if (data.getProcessingTimeSecond() != null) {
            evaluation.setProcessingTimeSecond(data.getProcessingTimeSecond());
        }
        if (data.getAiModelVersion() != null) {
            evaluation.setAiModelVersion(data.getAiModelVersion());
        }
        evaluation.setEvaluationType(EvaluationType.DETAIL);
        evaluation.setEvaluationStatus(EvaluationStatus.FINISH);
        resumeEvaluationRepository.save(evaluation);

        // Supplement existing criteria scores with aiExplanation, details, and suggestions
        supplementCriteriaScores(data.getCriteriaScores(), evaluation);
    }

    private boolean hasExistingCriteriaScores(ResumeEvaluation evaluation) {
        return evaluation.getCriteriaScores() != null && !evaluation.getCriteriaScores().isEmpty();
    }

    /**
     * Build a map of overview scores to send as context for detail supplement AI request.
     */
    private Map<String, Object> buildOverviewScoresMap(ResumeEvaluation evaluation) {
        Map<String, Object> overviewScores = new HashMap<>();
        overviewScores.put("aiOverallScore", evaluation.getAiOverallScore());
        overviewScores.put("matchLevel", evaluation.getMatchLevel() != null ? evaluation.getMatchLevel().name() : null);
        overviewScores.put("summary", evaluation.getSummary());
        overviewScores.put("strengths", evaluation.getStrengths());
        overviewScores.put("weakness", evaluation.getWeakness());

        // Add criteria scores
        List<Map<String, Object>> criteriaScoresList = new ArrayList<>();
        if (evaluation.getCriteriaScores() != null) {
            for (EvaluationCriteriaScore cs : evaluation.getCriteriaScores()) {
                Map<String, Object> csMap = new HashMap<>();
                if (cs.getScoringCriteria() != null && cs.getScoringCriteria().getCriteria() != null) {
                    csMap.put("criteriaId", cs.getScoringCriteria().getCriteria().getId());
                }
                csMap.put("aiScore", cs.getAiScore());
                criteriaScoresList.add(csMap);
            }
        }
        overviewScores.put("criteriaScores", criteriaScoresList);

        return overviewScores;
    }

    /**
     * Supplement existing criteria scores with aiExplanation, details, and suggestions.
     * Matches by criteriaType from the AI response to existing DB records.
     */
    private void supplementCriteriaScores(List<CriteriaScoreData> criteriaScores,
                                          ResumeEvaluation evaluation) {
        if (criteriaScores == null) return;

        // Build lookup map: criteriaType -> existing EvaluationCriteriaScore
        Map<Integer, EvaluationCriteriaScore> existingScoresMap = new HashMap<>();
        if (evaluation.getCriteriaScores() != null) {
            for (EvaluationCriteriaScore cs : evaluation.getCriteriaScores()) {
                if (cs.getScoringCriteria() != null && cs.getScoringCriteria().getCriteria() != null) {
                    existingScoresMap.put(cs.getScoringCriteria().getCriteria().getId(), cs);
                }
            }
        }

        for (CriteriaScoreData csData : criteriaScores) {
            EvaluationCriteriaScore existing = existingScoresMap.get(csData.getId());
            if (existing != null) {
                // Supplement existing record with explanation
                if (csData.getAiExplanation() != null) {
                    existing.setAiExplanation(csData.getAiExplanation());
                }
                evaluationCriteriaScoreRepository.save(existing);

                // Save nested details and suggestions
                saveDetails(csData.getDetails(), existing);

            } else {
                log.warn("No existing criteria score found for scoring criteria={} in evaluationId={}",
                        csData.getId(), evaluation.getId());
            }
        }
    }

    private void saveCriteriaScores(List<CriteriaScoreData> criteriaScores, ResumeEvaluation evaluation) {
        if (criteriaScores == null) return;

        for (CriteriaScoreData csData : criteriaScores) {
            EvaluationCriteriaScore criteriaScore = matchingResultMapper.toCriteriaScore(csData);
            criteriaScore.setEvaluation(evaluation);

            // Link to existing ScoringCriteria by criteriaType
            if (csData.getId() != null && evaluation.getJob() != null) {
                evaluation.getJob().getScoringCriterias().stream()
                        .filter(sc -> sc.getCriteria() != null
                                && Objects.equals(sc.getCriteria().getId(), csData.getId()))
                        .findFirst()
                        .ifPresent(criteriaScore::setScoringCriteria);
            }

            criteriaScore = evaluationCriteriaScoreRepository.save(criteriaScore);

            // Save nested details and suggestions
            saveDetails(csData.getDetails(), criteriaScore);
        }
    }

    /**
     * Save CriteriaScoreDetailData items as EvaluationCriteriaDetail entities,
     * including their suggestions as EvaluationCriteriaSuggestion entities.
     * Uses cascade to persist — no separate repository needed.
     */
    private void saveDetails(List<CriteriaScoreDetailData> details, EvaluationCriteriaScore criteriaScore) {
        if (details == null || details.isEmpty()) return;

        // Ensure details set is initialized (MapStruct + @Builder may leave it null)
        if (criteriaScore.getDetails() == null) {
            criteriaScore.setDetails(new HashSet<>());
        }

        for (CriteriaScoreDetailData detailData : details) {
            EvaluationCriteriaDetail detail = EvaluationCriteriaDetail.builder()
                    .label(detailData.getLabel())
                    .status(detailData.getStatus())
                    .description(detailData.getDescription())
                    .requiredLevel(detailData.getRequiredLevel())
                    .candidateLevel(detailData.getCandidateLevel())
                    .isRequired(detailData.getIsRequired())
                    .context(detailData.getContext())
                    .impactScore(detailData.getImpactScore())
                    .evaluationCriteriaScore(criteriaScore)
                    .build();

            criteriaScore.getDetails().add(detail);

            // Save suggestions for this detail
            if (detailData.getSuggestions() != null) {
                if (detail.getSuggestions() == null) {
                    detail.setSuggestions(new HashSet<>());
                }
                for (String suggestionText : detailData.getSuggestions()) {
                    EvaluationCriteriaSuggestion suggestion = EvaluationCriteriaSuggestion.builder()
                            .suggestion(suggestionText)
                            .evaluationCriteriaDetail(detail)
                            .build();
                    detail.getSuggestions().add(suggestion);
                }
            }
        }

        // Cascade will persist details and suggestions when criteria score is saved
        evaluationCriteriaScoreRepository.save(criteriaScore);
    }

    private void publishMatchingRequest(
            ResumeEvaluation evaluation,
            Resume resume,
            Job job,
            EvaluationType evaluationType,
            Map<String, Object> overviewScores
    ) {
        MatchingRequestMessage message = matchingRequestMapper.buildMessage(evaluation, resume, job);
        message.setMatchingType(evaluationType.toString());
        message.setOverviewScores(overviewScores);

        UsageEvent usageEvent = createMatchingUsageEvent(job.getId(), resume.getId());
        message.setUsageEventId(usageEvent.getId());

        try {
            matchingRequestPublisher.publish(message);
        } catch (Exception exception) {
            handlePublishFailure(evaluation, usageEvent.getId(), exception);
        }
    }

    private UsageEvent createMatchingUsageEvent(Integer jobId, Integer resumeId) {
        return quotaService.consumeEventQuota(
                FeatureKey.MATCHING_SCORE,
                1,
                List.of(
                        new UsageContextModel(EventSource.JOB, jobId),
                        new UsageContextModel(EventSource.RESUME, resumeId)
                )
        );
    }

    private void handlePublishFailure(ResumeEvaluation evaluation, Integer usageEventId, Exception exception) {
        log.error("Failed to publish matching request for evaluationId={}", evaluation.getId(), exception);
        evaluation.setEvaluationStatus(EvaluationStatus.FAIL);
        resumeEvaluationRepository.save(evaluation);
        quotaService.markUsageEventFailed(usageEventId);
        throw new MatchingPublishException("Failed to publish matching request", exception);
    }

    private void markMatchingFailed(ResumeEvaluation evaluation, Integer usageEventId, String errorMessage) {
        evaluation.setEvaluationStatus(EvaluationStatus.FAIL);
        resumeEvaluationRepository.save(evaluation);
        quotaService.markUsageEventFailed(usageEventId);
        log.warn("Matching failed for evaluationId={}: {}", evaluation.getId(), errorMessage);
    }
}
