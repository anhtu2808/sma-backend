package com.sma.core.service.impl;

import com.sma.core.dto.message.matching.MatchingRequestMessage;
import com.sma.core.dto.message.matching.MatchingResultData;
import com.sma.core.dto.message.matching.MatchingResultMessage;
import com.sma.core.dto.response.resume.ResumeEvaluationResponse;
import com.sma.core.entity.*;
import com.sma.core.enums.*;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.evaluation.EvaluationResponseMapper;
import com.sma.core.mapper.evaluation.MatchingRequestMapper;
import com.sma.core.mapper.evaluation.MatchingResultMapper;
import com.sma.core.messaging.matching.MatchingRequestPublisher;
import com.sma.core.repository.*;
import com.sma.core.service.ResumeEvaluationService;
import com.sma.core.service.ResumeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    @Transactional
    public void processMatching(Integer jobId, Integer resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        if (!job.getStatus().equals(JobStatus.PUBLISHED)) {
            throw new AppException(ErrorCode.JOB_NOT_AVAILABLE);
        }
        if (!resume.getParseStatus().equals(ResumeParseStatus.FINISH)) {
            resumeService.parseResume(resumeId);
            throw new AppException(ErrorCode.RESUME_NOT_PARSED);
        }

        // Create evaluation with WAITING status
        ResumeEvaluation evaluation = ResumeEvaluation.builder()
                .resume(resume)
                .job(job)
                .evaluationStatus(EvaluationStatus.WAITING)
                .build();
        evaluation = resumeEvaluationRepository.save(evaluation);

        // Build & publish message using mapper
        MatchingRequestMessage message = matchingRequestMapper.buildMessage(evaluation, resume, job);
        matchingRequestPublisher.publish(message);

        log.info("Matching request sent for evaluationId={}, jobId={}, resumeId={}",
                evaluation.getId(), jobId, resumeId);
    }

    @Override
    public String getMatchingStatus(Integer id) {
        ResumeEvaluation evaluation = resumeEvaluationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_NOT_EXISTED));
        return evaluation.getEvaluationStatus().name();
    }

    @Override
    public ResumeEvaluationResponse getMatchingResult(Integer id) {
        ResumeEvaluation evaluation = resumeEvaluationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_NOT_EXISTED));
        return evaluationResponseMapper.toEvaluationResponse(evaluation);
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

            // Map top-level fields directly from typed DTO
            matchingResultMapper.mapToEvaluation(data, evaluation);
            resumeEvaluationRepository.save(evaluation);

            // Save criteria scores with nested entities
            saveCriteriaScores(data.getCriteriaScores(), evaluation);

            // Save gaps
            saveGaps(data.getGaps(), evaluation);

            // Save weaknesses
            saveWeaknesses(data.getWeaknesses(), evaluation);

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
        // TODO: Implement suggestion generation using AI in a future iteration
        log.info("generateSuggestion called for evaluationId={} - not yet implemented", id);
    }

    @Override
    public void reGenerateSuggestion(Integer id, Integer evaluationWeaknessId) {
        // TODO: Implement suggestion re-generation using AI in a future iteration
        log.info("reGenerateSuggestion called for evaluationId={}, weaknessId={} - not yet implemented",
                id, evaluationWeaknessId);
    }

    // ---- Private helpers ----

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
