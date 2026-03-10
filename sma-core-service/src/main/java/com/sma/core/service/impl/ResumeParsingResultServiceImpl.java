package com.sma.core.service.impl;

import com.sma.core.dto.message.resume.ResumeParsingResultMessage;
import com.sma.core.dto.message.resume.parsed.*;
import com.sma.core.entity.*;
import com.sma.core.enums.ResumeParseStatus;
import com.sma.core.enums.ResumeStatus;
import com.sma.core.mapper.resume.ParsedResumeMapper;
import com.sma.core.repository.*;
import com.sma.core.service.NotificationService;
import com.sma.core.service.QuotaService;
import com.sma.core.service.ResumeParsingResultService;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeParsingResultServiceImpl implements ResumeParsingResultService {
    static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    final ResumeRepository resumeRepository;
    final SkillCategoryRepository skillCategoryRepository;
    final SkillRepository skillRepository;
    final ResumeSkillRepository resumeSkillRepository;
    final ResumeSkillGroupRepository resumeSkillGroupRepository;
    final ResumeEducationRepository resumeEducationRepository;
    final ResumeExperienceRepository resumeExperienceRepository;
    final ResumeExperienceDetailRepository resumeExperienceDetailRepository;
    final ExperienceSkillRepository experienceSkillRepository;
    final ResumeProjectRepository resumeProjectRepository;
    final ProjectSkillRepository projectSkillRepository;
    final ResumeCertificationRepository resumeCertificationRepository;
    final PlatformTransactionManager transactionManager;
    final ParsedResumeMapper parsedResumeMapper;

    final ConcurrentMap<String, Object> skillLocks = new ConcurrentHashMap<>();

    TransactionTemplate newTransactionTemplate;
    final NotificationService notificationService;
    final QuotaService quotaService;

    @PostConstruct
    void initTransactionTemplates() {
        newTransactionTemplate = new TransactionTemplate(transactionManager);
        newTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void processParsingResult(ResumeParsingResultMessage message) {
        if (message == null || message.getResumeId() == null) {
            if (message != null) {
                quotaService.markUsageEventFailed(message.getUsageEventId());
            }
            throw new AppException(ErrorCode.RESUME_PARSE_INVALID_MESSAGE);
        }

        Integer resumeId = message.getResumeId();
        Resume resume = resumeRepository.findById(resumeId).orElse(null);
        if (resume == null) {
            quotaService.markUsageEventFailed(message.getUsageEventId());
            throw new AppException(ErrorCode.RESUME_PARSE_NOT_FOUND);
        }

        String parseAttemptId = normalizeFreeText(message.getParseAttemptId());
        if (parseAttemptId == null) {
            markParseFailed(
                    resumeId,
                    message.getUsageEventId(),
                    normalizeFreeText(resume.getParseAttemptId()),
                    "INVALID_RESULT_MESSAGE: missing parseAttemptId",
                    message.getProcessedAt()
            );
            return;
        }

        if (!isAttemptMatched(resume, parseAttemptId)) {
            quotaService.markUsageEventFailed(message.getUsageEventId());
            throw new AppException(ErrorCode.RESUME_PARSE_STALE_ATTEMPT);
        }

        ResumeParseStatus messageStatus = message.getStatus();
        if (messageStatus == null) {
            markParseFailed(
                    resumeId,
                    message.getUsageEventId(),
                    parseAttemptId,
                    "INVALID_RESULT_MESSAGE: missing status",
                    message.getProcessedAt()
            );
            return;
        }

        if (messageStatus == ResumeParseStatus.FAIL) {
            markParseFailed(
                    resumeId,
                    message.getUsageEventId(),
                    parseAttemptId,
                    message.getErrorMessage(),
                    message.getProcessedAt()
            );
            return;
        }

        if (messageStatus != ResumeParseStatus.FINISH) {
            markParseFailed(
                    resumeId,
                    message.getUsageEventId(),
                    parseAttemptId,
                    "INVALID_RESULT_STATUS: " + messageStatus,
                    message.getProcessedAt()
            );
            return;
        }

        if (message.getParsedData() == null) {
            markParseFailed(
                    resumeId,
                    message.getUsageEventId(),
                    parseAttemptId,
                    "INVALID_RESULT_MESSAGE: missing parsedData for FINISH status",
                    message.getProcessedAt()
            );
            return;
        }

        try {
            saveParsedData(
                    resumeId,
                    message.getUsageEventId(),
                    parseAttemptId,
                    message.getParsedData(),
                    message.getProcessedAt()
            );
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.RESUME_PARSE_STALE_ATTEMPT) {
                quotaService.markUsageEventFailed(message.getUsageEventId());
            }
            throw e;
        } catch (Exception exception) {
            markParseFailed(
                    resumeId,
                    message.getUsageEventId(),
                    parseAttemptId,
                    exception.getMessage(),
                    message.getProcessedAt()
            );
            throw exception;
        }
    }

    private void saveParsedData(
            Integer resumeId,
            Integer usageEventId,
            String parseAttemptId,
            ParsedResumePayload payload,
            String processedAt
    ) {
        runInNewTransaction(() -> {
            Resume resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESUME_PARSE_NOT_FOUND));

            if (!isAttemptMatched(resume, parseAttemptId) || !isParseInProgress(resume)) {
                quotaService.markUsageEventFailed(usageEventId);
                throw new AppException(ErrorCode.RESUME_PARSE_STALE_ATTEMPT);
            }

            Map<String, SkillCategory> skillCategoryByName = new HashMap<>();
            Map<String, Skill> skillByName = new HashMap<>();

            clearExistingParsedData(resumeId);
            updateResumeFromParsed(resume, payload.getResume(), payload.getMetadata());
            saveResumeSkills(resume, payload.getResumeSkills(), skillCategoryByName, skillByName);
            saveResumeEducations(resume, payload.getResumeEducations());
            saveResumeExperiences(resume, payload.getResumeExperiences(), skillCategoryByName, skillByName);
            saveResumeProjects(resume, payload.getResumeProjects(), skillCategoryByName, skillByName);
            saveResumeCertifications(resume, payload.getResumeCertifications());

            resume.setStatus(ResumeStatus.ACTIVE);
            resume.setParseStatus(ResumeParseStatus.FINISH);
            resume.setParseErrorMessage(null);
            resume.setParseUpdatedAt(resolveProcessedAt(processedAt));
            resumeRepository.save(resume);
        });
    }

    @Override
    public void markParseFailed(
            Integer resumeId,
            Integer usageEventId,
            String parseAttemptId,
            String errorMessage,
            String processedAt
    ) {
        runInNewTransaction(() -> {
            Resume resume = resumeRepository.findById(resumeId).orElse(null);
            if (resume == null) {
                quotaService.markUsageEventFailed(usageEventId);
                return;
            }

            if (parseAttemptId != null && !isAttemptMatched(resume, parseAttemptId)) {
                quotaService.markUsageEventFailed(usageEventId);
                return;
            }

            if (!isParseInProgress(resume)) {
                return;
            }

            resume.setStatus(ResumeStatus.DRAFT);
            resume.setParseStatus(ResumeParseStatus.FAIL);
            resume.setParseErrorMessage(normalizeErrorMessage(errorMessage));
            resume.setParseUpdatedAt(resolveProcessedAt(processedAt));
            resumeRepository.save(resume);
            quotaService.markUsageEventFailed(usageEventId);

            try {
                notificationService.sendCandidateNotification(
                        resume.getCandidate().getUser(),
                        com.sma.core.enums.NotificationType.CV_PARSE_FAILED,
                        "CV Parsing Failed",
                        "We couldn't read your CV file: " + resume.getFileName() + ". Please re-upload a clear file.",
                        "RESUME",
                        resumeId
                );
            } catch (Exception notificationException) {
                log.warn("Failed to send parse-failed notification for resumeId={}", resumeId, notificationException);
            }
        });
    }

    private void runInNewTransaction(Runnable runnable) {
        newTransactionTemplate.executeWithoutResult(status -> runnable.run());
    }

    private boolean isAttemptMatched(Resume resume, String parseAttemptId) {
        if (resume == null) {
            return false;
        }
        String currentAttemptId = normalizeFreeText(resume.getParseAttemptId());
        String incomingAttemptId = normalizeFreeText(parseAttemptId);
        if (currentAttemptId == null || incomingAttemptId == null) {
            return false;
        }
        return currentAttemptId.equals(incomingAttemptId);
    }

    private boolean isParseInProgress(Resume resume) {
        return resume != null && resume.getParseStatus() == ResumeParseStatus.PARTIAL;
    }

    private void clearExistingParsedData(Integer resumeId) {
        projectSkillRepository.deleteByResumeId(resumeId);
        experienceSkillRepository.deleteByResumeId(resumeId);
        resumeExperienceDetailRepository.deleteByResumeId(resumeId);
        resumeProjectRepository.deleteByResumeId(resumeId);
        resumeExperienceRepository.deleteByResumeId(resumeId);
        resumeSkillRepository.deleteByResumeId(resumeId);
        resumeSkillGroupRepository.deleteByResumeId(resumeId);
        resumeEducationRepository.deleteByResumeId(resumeId);
        resumeCertificationRepository.deleteByResumeId(resumeId);
    }

    private void updateResumeFromParsed(
            Resume resume,
            ParsedResume parsedResume,
            ParsedMetadata metadata
    ) {
        if (parsedResume == null) {
            return;
        }

        setIfNotBlank(resume::setResumeName, parsedResume.getResumeName());
        setIfNotBlank(resume::setFileName, parsedResume.getFileName());
        setIfNotBlank(resume::setRawText, parsedResume.getRawText());
        setIfNotBlank(resume::setAddressInResume, parsedResume.getAddressInResume());
        setIfNotBlank(resume::setPhoneInResume, parsedResume.getPhoneInResume());
        setIfNotBlank(resume::setEmailInResume, parsedResume.getEmailInResume());
        setIfNotBlank(resume::setGithubLink, parsedResume.getGithubLink());
        setIfNotBlank(resume::setLinkedinLink, parsedResume.getLinkedinLink());
        setIfNotBlank(resume::setPortfolioLink, parsedResume.getPortfolioLink());
        setIfNotBlank(resume::setFullName, parsedResume.getFullName());
        setIfNotBlank(resume::setAvatar, parsedResume.getAvatar());
        setIfNotBlank(resume::setResumeUrl, parsedResume.getResumeUrl());

        if (parsedResume.getLanguage() != null) {
            resume.setLanguage(parsedResume.getLanguage());
        } else if (metadata != null && metadata.getResumeLanguage() != null) {
            resume.setLanguage(metadata.getResumeLanguage());
        }

        if (parsedResume.getIsDefault() != null) {
            resume.setIsDefault(parsedResume.getIsDefault());
        }
    }

    private void saveResumeSkills(
            Resume resume,
            List<ParsedResumeSkillGroup> groupedSkills,
            Map<String, SkillCategory> skillCategoryByName,
            Map<String, Skill> skillByName
    ) {
        Map<String, ResumeSkillGroup> skillGroupByName = new HashMap<>();
        List<ParsedResumeSkillGroup> groups = orEmptyList(groupedSkills);

        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            ParsedResumeSkillGroup group = groups.get(groupIndex);
            String groupName = firstNonBlank(group != null ? group.getGroupName() : null, "Ungrouped");
            Integer groupOrderIndex = resolveOrderIndex(group != null ? group.getOrderIndex() : null, groupIndex + 1);
            String groupKey = normalizeLookupKey(groupName);

            ResumeSkillGroup skillGroup = skillGroupByName.get(groupKey);
            if (skillGroup == null) {
                skillGroup = ResumeSkillGroup.builder()
                        .name(groupName)
                        .orderIndex(groupOrderIndex)
                        .resume(resume)
                        .build();
                skillGroup = resumeSkillGroupRepository.save(skillGroup);
                skillGroupByName.put(groupKey, skillGroup);
            }

            List<ParsedResumeSkill> skills = group == null ? List.of() : orEmptyList(group.getSkills());
            for (ParsedResumeSkill skillItem : skills) {
                Skill skill = findOrCreateSkill(
                        skillItem.getSkillName(),
                        skillItem.getDescription(),
                        null,
                        skillCategoryByName,
                        skillByName
                );
                if (skill == null) {
                    continue;
                }

                ResumeSkill resumeSkill = ResumeSkill.builder()
                        .yearsOfExperience(skillItem.getYearsOfExperience())
                        .skillGroup(skillGroup)
                        .skill(skill)
                        .build();
                resumeSkillRepository.save(resumeSkill);
            }
        }
    }

    private void saveResumeEducations(Resume resume, List<ParsedResumeEducation> educations) {
        List<ParsedResumeEducation> values = orEmptyList(educations);
        for (int index = 0; index < values.size(); index++) {
            ParsedResumeEducation educationValue = values.get(index);
            if (educationValue == null) {
                continue;
            }

            ResumeEducation education = parsedResumeMapper.toEducation(educationValue);
            education.setResume(resume);
            education.setOrderIndex(resolveOrderIndex(educationValue.getOrderIndex(), index + 1));
            resumeEducationRepository.save(education);
        }
    }

    private void saveResumeExperiences(
            Resume resume,
            List<ParsedResumeExperience> experiences,
            Map<String, SkillCategory> skillCategoryByName,
            Map<String, Skill> skillByName
    ) {
        List<ParsedResumeExperience> values = orEmptyList(experiences);
        for (int experienceIndex = 0; experienceIndex < values.size(); experienceIndex++) {
            ParsedResumeExperience parsedExperience = values.get(experienceIndex);
            if (parsedExperience == null) {
                continue;
            }

            ResumeExperience experience = parsedResumeMapper.toExperience(parsedExperience);
            experience.setResume(resume);
            experience.setOrderIndex(resolveOrderIndex(parsedExperience.getOrderIndex(), experienceIndex + 1));
            experience = resumeExperienceRepository.save(experience);

            List<ParsedResumeExperienceDetail> details = orEmptyList(parsedExperience.getDetails());
            for (int detailIndex = 0; detailIndex < details.size(); detailIndex++) {
                ParsedResumeExperienceDetail parsedDetail = details.get(detailIndex);
                if (parsedDetail == null) {
                    continue;
                }

                ResumeExperienceDetail detail = parsedResumeMapper.toExperienceDetail(parsedDetail);
                detail.setExperience(experience);
                detail.setOrderIndex(resolveOrderIndex(parsedDetail.getOrderIndex(), detailIndex + 1));
                detail = resumeExperienceDetailRepository.save(detail);

                List<ParsedExperienceSkill> detailSkills = orEmptyList(parsedDetail.getSkills());
                for (ParsedExperienceSkill detailSkill : detailSkills) {
                    Skill skill = findOrCreateSkill(
                            detailSkill.getSkillName(),
                            detailSkill.getDescription(),
                            null,
                            skillCategoryByName,
                            skillByName
                    );
                    if (skill == null) {
                        continue;
                    }

                    ExperienceSkill experienceSkill = ExperienceSkill.builder()
                            .description(detailSkill.getDescription())
                            .detail(detail)
                            .skill(skill)
                            .build();
                    experienceSkillRepository.save(experienceSkill);
                }
            }
        }
    }

    private void saveResumeProjects(
            Resume resume,
            List<ParsedResumeProject> projects,
            Map<String, SkillCategory> skillCategoryByName,
            Map<String, Skill> skillByName
    ) {
        List<ParsedResumeProject> values = orEmptyList(projects);
        for (int projectIndex = 0; projectIndex < values.size(); projectIndex++) {
            ParsedResumeProject parsedProject = values.get(projectIndex);
            if (parsedProject == null) {
                continue;
            }

            ResumeProject project = parsedResumeMapper.toProject(parsedProject);
            project.setResume(resume);
            project.setOrderIndex(resolveOrderIndex(parsedProject.getOrderIndex(), projectIndex + 1));
            project = resumeProjectRepository.save(project);

            List<ParsedProjectSkill> projectSkills = orEmptyList(parsedProject.getSkills());
            for (ParsedProjectSkill parsedProjectSkill : projectSkills) {
                Skill skill = findOrCreateSkill(
                        parsedProjectSkill.getSkillName(),
                        parsedProjectSkill.getDescription(),
                        null,
                        skillCategoryByName,
                        skillByName
                );
                if (skill == null) {
                    continue;
                }

                ProjectSkill projectSkill = ProjectSkill.builder()
                        .description(parsedProjectSkill.getDescription())
                        .project(project)
                        .skill(skill)
                        .build();
                projectSkillRepository.save(projectSkill);
            }
        }
    }

    private void saveResumeCertifications(Resume resume, List<ParsedResumeCertification> certifications) {
        for (ParsedResumeCertification parsedCertification : orEmptyList(certifications)) {
            if (parsedCertification == null) {
                continue;
            }

            ResumeCertification certification = parsedResumeMapper.toCertification(parsedCertification);
            certification.setResume(resume);
            resumeCertificationRepository.save(certification);
        }
    }

    private Skill findOrCreateSkill(
            String rawSkillName,
            String skillDescription,
            String rawCategoryName,
            Map<String, SkillCategory> skillCategoryByName,
            Map<String, Skill> skillByName
    ) {
        String skillName = normalizeFreeText(rawSkillName);
        if (skillName == null) {
            return null;
        }

        SkillCategory category = resolveSkillCategory(rawCategoryName, skillCategoryByName);
        String lookupKey = normalizeLookupKey(skillName);

        Skill existingSkill = skillByName.get(lookupKey);
        if (existingSkill != null) {
            return existingSkill;
        }

        Object lock = skillLocks.computeIfAbsent(lookupKey, ignored -> new Object());
        synchronized (lock) {
            existingSkill = skillByName.get(lookupKey);
            if (existingSkill != null) {
                return existingSkill;
            }

            List<Skill> existingSkills = skillRepository.findAllByNormalizedName(skillName);
            Skill skill;
            if (!existingSkills.isEmpty()) {
                skill = existingSkills.get(0);
                if (existingSkills.size() > 1) {
                    throw new AppException(ErrorCode.RESUME_PARSE_DUPLICATE_SKILL);
                }
            } else {
                skill = skillRepository.save(
                        Skill.builder()
                                .name(skillName)
                                .description(skillDescription)
                                .category(category)
                                .build()
                );
            }

            if (category != null && skill.getCategory() == null) {
                skill.setCategory(category);
            }

            if ((skill.getDescription() == null || skill.getDescription().isBlank())
                    && skillDescription != null
                    && !skillDescription.isBlank()) {
                skill.setDescription(skillDescription);
            }
            skill = skillRepository.save(skill);

            skillByName.put(lookupKey, skill);
            return skill;
        }
    }

    private SkillCategory resolveSkillCategory(String rawCategoryName, Map<String, SkillCategory> skillCategoryByName) {
        String categoryName = normalizeFreeText(rawCategoryName);
        if (categoryName == null) {
            return null;
        }

        String lookupKey = normalizeLookupKey(categoryName);
        if (skillCategoryByName.containsKey(lookupKey)) {
            return skillCategoryByName.get(lookupKey);
        }

        List<SkillCategory> existingCategories = skillCategoryRepository.findAllByNormalizedName(categoryName);
        SkillCategory existingCategory = existingCategories.isEmpty() ? null : existingCategories.get(0);
        if (existingCategories.size() > 1) {
            throw new AppException(ErrorCode.RESUME_PARSE_DUPLICATE_CATEGORY);
        }

        skillCategoryByName.put(lookupKey, existingCategory);
        return existingCategory;
    }

    private String normalizeFreeText(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String normalized = rawValue.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeLookupKey(String rawValue) {
        String normalized = normalizeFreeText(rawValue);
        return normalized == null ? "" : normalized.toLowerCase(Locale.ROOT);
    }

    private Integer resolveOrderIndex(Integer rawOrderIndex, int fallbackOrderIndex) {
        if (rawOrderIndex == null || rawOrderIndex <= 0) {
            return fallbackOrderIndex;
        }
        return rawOrderIndex;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = normalizeFreeText(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private LocalDateTime resolveProcessedAt(String rawProcessedAt) {
        String processedAt = normalizeFreeText(rawProcessedAt);
        if (processedAt == null) {
            return LocalDateTime.now();
        }

        try {
            return OffsetDateTime.parse(processedAt).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return Instant.parse(processedAt).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        return LocalDateTime.now();
    }

    private String normalizeErrorMessage(String rawErrorMessage) {
        String value = normalizeFreeText(rawErrorMessage);
        if (value == null) {
            return "PARSE_FAILED";
        }

        if (value.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    private <T> List<T> orEmptyList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private void setIfNotBlank(Consumer<String> setter, String value) {
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }
}
