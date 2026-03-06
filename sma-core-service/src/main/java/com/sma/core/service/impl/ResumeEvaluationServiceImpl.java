package com.sma.core.service.impl;

import com.sma.core.dto.message.matching.MatchingRequestMessage;
import com.sma.core.dto.message.matching.MatchingResultData;
import com.sma.core.dto.message.matching.MatchingResultMessage;
import com.sma.core.dto.message.suggest.ReSuggestRequestMessage;
import com.sma.core.dto.message.suggest.SuggestResultMessage;
import com.sma.core.dto.message.suggest.SuggestionRequestMessage;
import com.sma.core.dto.request.evaluation.ManualScoreMatchingRequest;
import com.sma.core.dto.request.evaluation.suggest.WeaknessSuggestionRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.evaluation.ResumeEvaluationDetailResponse;
import com.sma.core.dto.response.evaluation.ResumeEvaluationOverviewResponse;
import com.sma.core.dto.response.suggestion.GapSuggestionResponse;
import com.sma.core.dto.response.suggestion.WeaknessSuggestionResponse;
import com.sma.core.entity.*;
import com.sma.core.enums.*;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
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
    EvaluationExperienceDetailRepository evaluationExperienceDetailRepository;
    EvaluationHardSkillRepository evaluationHardSkillRepository;
    EvaluationWeaknessRepository evaluationWeaknessRepository;
    EvaluationGapRepository evaluationGapRepository;
    EvaluationSoftSkillRepository evaluationSoftSkillRepository;
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

    @Override
    @Transactional
    public Integer processMatching(Integer jobId, Integer resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        validateForMatching(job, resume);
        quotaService.checkEventQuotaAvailability(FeatureKey.MATCHING_SCORE);
        // Check if an overview evaluation already exists for this (job, resume) pair
        Optional<ResumeEvaluation> existingOverview = resumeEvaluationRepository
                .findByResumeIdAndJobId(resumeId, jobId);

        if (existingOverview.isPresent() && existingOverview.get().getEvaluationStatus() == EvaluationStatus.FINISH) {
            // Supplement mode: reuse overview record, only request gaps/explanations/skills
            ResumeEvaluation evaluation = existingOverview.get();
            evaluation.setEvaluationType(EvaluationType.DETAIL);
            evaluation.setEvaluationStatus(EvaluationStatus.WAITING);
            resumeEvaluationRepository.save(evaluation);

            // Build message with overview scores as context for AI
            MatchingRequestMessage message = matchingRequestMapper.buildMessage(evaluation, resume, job);
            message.setMatchingType(EvaluationType.DETAIL.toString());
            message.setOverviewScores(buildOverviewScoresMap(evaluation));
            matchingRequestPublisher.publish(message);

            log.info("Detail supplement request sent for evaluationId={}, jobId={}, resumeId={}",
                    evaluation.getId(), jobId, resumeId);
            return evaluation.getId();
        } else {
            // Full detail mode: no overview exists, create new record and do full matching
            return doProcessMatching(jobId, resumeId, EvaluationType.DETAIL, resume, job);
        }
    }

    @Override
    @Transactional
    public Integer processMatchingOverview(Integer jobId, Integer resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        validateForMatching(job, resume);
        if (job.getEnableAiScoring()) {
            quotaService.checkEventQuotaAvailability(FeatureKey.MATCHING_SCORE, Role.RECRUITER, job.getCompany().getId());
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
                .orElse(null);
        if (evaluation == null) {
            log.error("Evaluation not found for id={}", message.getEvaluationId());
            return;
        }

        if (message.getStatus() == EvaluationStatus.FAIL) {
            evaluation.setEvaluationStatus(EvaluationStatus.FAIL);
            resumeEvaluationRepository.save(evaluation);
            log.warn("Matching failed for evaluationId={}: {}", message.getEvaluationId(), message.getErrorMessage());
            return;
        }

        try {
            MatchingResultData data = message.getParsedData();
            if (data == null) {
                evaluation.setEvaluationStatus(EvaluationStatus.FAIL);
                resumeEvaluationRepository.save(evaluation);
                return;
            }

            if (evaluation.getEvaluationType() == EvaluationType.DETAIL
                    && hasExistingCriteriaScores(evaluation)) {
                // Detail supplement mode: only add explanations, nested skills, gaps, weaknesses
                processDetailSupplementResult(data, evaluation);
            } else {
                // Overview or full detail mode: save everything from scratch
                processFullResult(data, evaluation);
            }

            log.info("Successfully processed matching result for evaluationId={}, overallScore={}",
                    evaluation.getId(), evaluation.getAiOverallScore());

        } catch (Exception e) {
            log.error("Error processing matching result for evaluationId={}", message.getEvaluationId(), e);
            evaluation.setEvaluationStatus(EvaluationStatus.FAIL);
            resumeEvaluationRepository.save(evaluation);
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
    public void reGenerateSuggestion(Integer id, Integer evaluationWeaknessId) {
        ResumeEvaluation evaluation = resumeEvaluationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_NOT_EXISTED));
        EvaluationWeakness weakness = evaluationWeaknessRepository.findByIdAndEvaluationId(evaluationWeaknessId, id)
                .orElseThrow(() -> new AppException(ErrorCode.WEAKNESS_NOT_FOUND));
        ReSuggestRequestMessage message = evaluationMapper.toReSuggestRequestMessage(evaluation);
        message.setWeakness(evaluationMapper.toWeaknessSuggestionRequest(weakness));
        suggestionRequestPublisher.publish(message);
        log.info("reGenerateSuggestion called for evaluationId={}, weaknessId={} - not yet implemented",
                id, evaluationWeaknessId);
    }

    @Transactional
    @Override
    public void saveSuggestion(SuggestResultMessage message) {

        resumeEvaluationRepository.findById(message.getEvaluationId())
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_NOT_EXISTED));

        Map<Integer, String> gapSuggestions =
                message.getGapSuggestion().stream()
                        .collect(Collectors.toMap(
                                GapSuggestionResponse::getId,
                                GapSuggestionResponse::getSuggestion
                        ));

        evaluationGapRepository
                .findAllById(gapSuggestions.keySet())
                .forEach(gap ->
                        gap.setSuggestion(gapSuggestions.get(gap.getId()))
                );

        Map<Integer, String> weaknessSuggestions =
                message.getWeaknessSuggestion().stream()
                        .collect(Collectors.toMap(
                                WeaknessSuggestionResponse::getId,
                                WeaknessSuggestionResponse::getSuggestion
                        ));

        evaluationWeaknessRepository
                .findAllById(weaknessSuggestions.keySet())
                .forEach(w ->
                        w.setSuggestion(weaknessSuggestions.get(w.getId()))
                );
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
        if (!resume.getParseStatus().equals(ResumeParseStatus.FINISH)) {
            resumeService.parseResume(resume.getId());
            throw new AppException(ErrorCode.RESUME_NOT_PARSED);
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

        // Build & publish message using mapper
        MatchingRequestMessage message = matchingRequestMapper.buildMessage(evaluation, resume, job);
        message.setMatchingType(type.toString());
        matchingRequestPublisher.publish(message);

        log.info("Matching request sent for evaluationId={}, jobId={}, resumeId={}, matchingType={}",
                evaluation.getId(), jobId, resumeId, type);

        return evaluation.getId();
    }

    /**
     * Process full matching result — used for overview and full detail (no prior overview).
     * Saves top-level fields, criteria scores, and optionally nested skills/gaps/weaknesses.
     */
    private void processFullResult(MatchingResultData data, ResumeEvaluation evaluation) {
        // Map top-level fields
        matchingResultMapper.mapToEvaluation(data, evaluation);
        resumeEvaluationRepository.save(evaluation);

        // Save criteria scores
        saveCriteriaScores(data.getCriteriaScores(), evaluation);

        // Save gaps and weaknesses (null-safe — overview results won't have these)
        saveGaps(data.getGaps(), evaluation);
        saveWeaknesses(data.getWeaknesses(), evaluation);
    }

    /**
     * Process detail supplement result — used when overview already exists.
     * Only adds aiExplanation, nested skills, gaps, and weaknesses to existing record.
     * Does NOT re-score criteria.
     */
    private void processDetailSupplementResult(MatchingResultData data, ResumeEvaluation evaluation) {
        // Update supplementary top-level fields (isTrueLevel, hasRelatedExperience)
        if (data.getIsTrueLevel() != null) {
            evaluation.setIsTrueLevel(data.getIsTrueLevel());
        }
        if (data.getHasRelatedExperience() != null) {
            evaluation.setHasRelatedExperience(data.getHasRelatedExperience());
        }
        if (data.getProcessingTimeSecond() != null) {
            evaluation.setProcessingTimeSecond(data.getProcessingTimeSecond());
        }
        evaluation.setEvaluationStatus(EvaluationStatus.FINISH);
        resumeEvaluationRepository.save(evaluation);

        // Supplement existing criteria scores with aiExplanation and nested skills
        supplementCriteriaScores(data.getCriteriaScores(), evaluation);

        // Save new gaps and weaknesses
        saveGaps(data.getGaps(), evaluation);
        saveWeaknesses(data.getWeaknesses(), evaluation);
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
                    csMap.put("criteriaType", cs.getScoringCriteria().getCriteria().getCriteriaType().name());
                }
                csMap.put("aiScore", cs.getAiScore());
                csMap.put("weightedScore", cs.getWeightedScore());
                criteriaScoresList.add(csMap);
            }
        }
        overviewScores.put("criteriaScores", criteriaScoresList);

        return overviewScores;
    }

    /**
     * Supplement existing criteria scores with aiExplanation and nested skill details.
     * Matches by criteriaType from the AI response to existing DB records.
     */
    private void supplementCriteriaScores(List<MatchingResultData.CriteriaScoreData> criteriaScores,
                                          ResumeEvaluation evaluation) {
        if (criteriaScores == null) return;

        // Build lookup map: criteriaType -> existing EvaluationCriteriaScore
        Map<CriteriaType, EvaluationCriteriaScore> existingScoresMap = new HashMap<>();
        if (evaluation.getCriteriaScores() != null) {
            for (EvaluationCriteriaScore cs : evaluation.getCriteriaScores()) {
                if (cs.getScoringCriteria() != null && cs.getScoringCriteria().getCriteria() != null) {
                    existingScoresMap.put(cs.getScoringCriteria().getCriteria().getCriteriaType(), cs);
                }
            }
        }

        for (MatchingResultData.CriteriaScoreData csData : criteriaScores) {
            EvaluationCriteriaScore existing = existingScoresMap.get(csData.getCriteriaType());
            if (existing != null) {
                // Supplement existing record with explanation and nested details
                if (csData.getAiExplanation() != null) {
                    existing.setAiExplanation(csData.getAiExplanation());
                }
                evaluationCriteriaScoreRepository.save(existing);

                // Save nested details
                saveHardSkills(csData.getHardSkills(), existing);
                saveSoftSkills(csData.getSoftSkills(), existing);
                saveExperienceDetails(csData.getExperienceDetails(), existing);
            } else {
                log.warn("No existing criteria score found for criteriaType={} in evaluationId={}",
                        csData.getCriteriaType(), evaluation.getId());
            }
        }
    }

    private void saveCriteriaScores(List<MatchingResultData.CriteriaScoreData> criteriaScores, ResumeEvaluation evaluation) {
        if (criteriaScores == null) return;

        for (MatchingResultData.CriteriaScoreData csData : criteriaScores) {
            EvaluationCriteriaScore criteriaScore = matchingResultMapper.toCriteriaScore(csData);
            criteriaScore.setEvaluation(evaluation);

            if (csData.getMaxScore() == null) {
                criteriaScore.setMaxScore(100f);
            }

            // Link to existing ScoringCriteria by criteriaType
            if (csData.getCriteriaType() != null && evaluation.getJob() != null) {
                evaluation.getJob().getScoringCriterias().stream()
                        .filter(sc -> sc.getCriteria() != null
                                && sc.getCriteria().getCriteriaType() == csData.getCriteriaType())
                        .findFirst()
                        .ifPresent(criteriaScore::setScoringCriteria);
            }

            criteriaScore = evaluationCriteriaScoreRepository.save(criteriaScore);

            // Save nested details (null-safe — overview results won't have these)
            saveHardSkills(csData.getHardSkills(), criteriaScore);
            saveSoftSkills(csData.getSoftSkills(), criteriaScore);
            saveExperienceDetails(csData.getExperienceDetails(), criteriaScore);
        }
    }

    private void saveHardSkills(List<MatchingResultData.HardSkillData> skills, EvaluationCriteriaScore criteriaScore) {
        if (skills == null) return;
        for (MatchingResultData.HardSkillData data : skills) {
            EvaluationHardSkill entity = matchingResultMapper.toHardSkill(data);
            entity.setEvaluationCriteriaScore(criteriaScore);
            evaluationHardSkillRepository.save(entity);
        }
    }

    private void saveSoftSkills(List<MatchingResultData.SoftSkillData> skills, EvaluationCriteriaScore criteriaScore) {
        if (skills == null) return;
        for (MatchingResultData.SoftSkillData data : skills) {
            EvaluationSoftSkill entity = matchingResultMapper.toSoftSkill(data);
            entity.setEvaluationCriteriaScore(criteriaScore);
            evaluationSoftSkillRepository.save(entity);
        }
    }

    private void saveExperienceDetails(List<MatchingResultData.ExperienceDetailData> details, EvaluationCriteriaScore criteriaScore) {
        if (details == null) return;
        for (MatchingResultData.ExperienceDetailData data : details) {
            EvaluationExperienceDetail entity = matchingResultMapper.toExperienceDetail(data);
            entity.setEvaluationCriteriaScore(criteriaScore);
            evaluationExperienceDetailRepository.save(entity);
        }
    }

    private void saveGaps(List<MatchingResultData.GapData> gaps, ResumeEvaluation evaluation) {
        if (gaps == null) return;
        for (MatchingResultData.GapData data : gaps) {
            EvaluationGap entity = matchingResultMapper.toGap(data);
            entity.setEvaluation(evaluation);
            evaluationGapRepository.save(entity);
        }
    }

    private void saveWeaknesses(List<MatchingResultData.WeaknessData> weaknesses, ResumeEvaluation evaluation) {
        if (weaknesses == null) return;
        for (MatchingResultData.WeaknessData data : weaknesses) {
            EvaluationWeakness entity = matchingResultMapper.toWeakness(data);
            entity.setEvaluation(evaluation);
            evaluationWeaknessRepository.save(entity);
        }
    }
}
