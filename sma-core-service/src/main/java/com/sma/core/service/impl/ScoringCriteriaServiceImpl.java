package com.sma.core.service.impl;

import com.sma.core.dto.message.criteria.CriteriaContextRequestMessage;
import com.sma.core.dto.request.job.AddJobScoringCriteriaRequest;
import com.sma.core.entity.*;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.messaging.criteria.CriteriaContextRequestPublisher;
import com.sma.core.repository.CriteriaRepository;
import com.sma.core.repository.ScoringCriteriaRepository;
import com.sma.core.service.ScoringCriteriaService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ScoringCriteriaServiceImpl implements ScoringCriteriaService {

    final CriteriaRepository criteriaRepository;
    final ScoringCriteriaRepository scoringCriteriaRepository;
    final CriteriaContextRequestPublisher criteriaContextRequestPublisher;

    @Override
    public Set<ScoringCriteria> saveJobScoringCriteria(
            Job job,
            Set<AddJobScoringCriteriaRequest> requests) {

        Set<ScoringCriteria> result = new HashSet<>();

        double totalWeight = 0.0;

        for (AddJobScoringCriteriaRequest request : requests) {
            Criteria criteria = criteriaRepository.findById(request.getCriteriaId())
                    .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_NOT_EXISTED));
            boolean enable = Boolean.TRUE.equals(request.getEnable());
            ScoringCriteria existing = job == null
                    ? null
                    : scoringCriteriaRepository
                    .findByCriteria_IdAndJob_Id(criteria.getId(), job.getId())
                    .orElse(null);
            if (!enable) {
                if (existing != null) {
                    scoringCriteriaRepository.delete(existing);
                }
                continue;
            }
            double weight = request.getWeight() != null
                    ? request.getWeight()
                    : criteria.getDefaultWeight();

            totalWeight += weight;
            if (existing == null) {

                ScoringCriteria sc = ScoringCriteria.builder()
                        .criteria(criteria)
                        .job(job)
                        .weight(weight)
                        .build();

                result.add(sc);

            } else {
                existing.setWeight(weight);
                result.add(existing);
            }
        }

        if (Math.abs(totalWeight - 100.0) > 0.001) {
            throw new AppException(ErrorCode.INVALID_TOTAL_WEIGHT);
        }

        return result;
    }

    @Override
    public void generateAndSetCriteriaContext(Job job) {
        if (job.getScoringCriterias() == null || job.getScoringCriterias().isEmpty()) return;

        List<String> enabledTypes = new ArrayList<>();
        Map<String, Integer> typeToId = new HashMap<>();

        for (ScoringCriteria sc : job.getScoringCriterias()) {
            if (sc.getCriteria() != null && sc.getCriteria().getCriteriaType() != null) {
                String typeName = sc.getCriteria().getCriteriaType().name();
                enabledTypes.add(typeName);
                typeToId.put(typeName, sc.getId());
            }
        }

        if (enabledTypes.isEmpty()) return;

        CriteriaContextRequestMessage message = CriteriaContextRequestMessage.builder()
                .jobId(job.getId())
                .jobName(job.getName())
                .about(job.getAbout())
                .responsibilities(job.getResponsibilities())
                .requirement(job.getRequirement())
                .jobLevel(job.getJobLevel() != null ? job.getJobLevel().name() : null)
                .experienceTime(job.getExperienceTime())
                .workingModel(job.getWorkingModel() != null ? job.getWorkingModel().name() : null)
                .skills(job.getSkills() != null
                        ? job.getSkills().stream().map(Skill::getName).collect(Collectors.toList())
                        : Collections.emptyList())
                .domains(job.getDomains() != null
                        ? job.getDomains().stream().map(Domain::getName).collect(Collectors.toList())
                        : Collections.emptyList())
                .criteriaTypes(enabledTypes)
                .criteriaTypeToScoringCriteriaId(typeToId)
                .build();

        criteriaContextRequestPublisher.publish(message);
    }
}
