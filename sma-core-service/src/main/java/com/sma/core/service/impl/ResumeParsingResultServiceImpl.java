package com.sma.core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.sma.core.dto.message.resume.ResumeParsingResultMessage;
import com.sma.core.entity.*;
import com.sma.core.enums.DegreeType;
import com.sma.core.enums.EmploymentType;
import com.sma.core.enums.ProjectType;
import com.sma.core.enums.ResumeLanguage;
import com.sma.core.enums.ResumeParseStatus;
import com.sma.core.enums.ResumeStatus;
import com.sma.core.enums.ResumeType;
import com.sma.core.enums.WorkingModel;
import com.sma.core.repository.*;
import com.sma.core.service.ResumeParsingResultService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ResumeParsingResultServiceImpl implements ResumeParsingResultService {
    ResumeRepository resumeRepository;
    SkillCategoryRepository skillCategoryRepository;
    SkillRepository skillRepository;
    ResumeSkillRepository resumeSkillRepository;
    ResumeSkillGroupRepository resumeSkillGroupRepository;
    ResumeEducationRepository resumeEducationRepository;
    ResumeExperienceRepository resumeExperienceRepository;
    ResumeExperienceDetailRepository resumeExperienceDetailRepository;
    ExperienceSkillRepository experienceSkillRepository;
    ResumeProjectRepository resumeProjectRepository;
    ProjectSkillRepository projectSkillRepository;
    ResumeCertificationRepository resumeCertificationRepository;
    ConcurrentMap<String, Object> skillUpsertLocks = new ConcurrentHashMap<>();

    @Override
    public void processParsingResult(ResumeParsingResultMessage message) {
        if (message == null || message.getResumeId() == null) {
            log.warn("Ignore invalid resume parsing result: {}", message);
            return;
        }

        Integer resumeId = message.getResumeId();
        Resume resume = resumeRepository.findById(resumeId).orElse(null);
        if (resume == null) {
            log.warn("Ignore resume parsing result because resume not found: resumeId={}", resumeId);
            return;
        }

        ResumeParseStatus messageStatus = message.getStatus();
        if (messageStatus == null) {
            log.warn("Ignore resume parsing result with null status for resumeId={}", resumeId);
            resume.setParseStatus(ResumeParseStatus.FAIL);
            resume.setStatus(ResumeStatus.DRAFT);
            resumeRepository.save(resume);
            return;
        }

        if (messageStatus != ResumeParseStatus.FINISH) {
            log.warn(
                    "Resume parsing not finished for resumeId={}, status={}, error={}",
                    resumeId,
                    messageStatus,
                    message.getErrorMessage()
            );
            resume.setParseStatus(messageStatus);
            if (messageStatus == ResumeParseStatus.FAIL) {
                resume.setStatus(ResumeStatus.DRAFT);
            }
            resumeRepository.save(resume);
            return;
        }

        JsonNode parsedData = message.getParsedData();
        if (parsedData == null || parsedData.isNull()) {
            log.warn("Ignore success result with empty parsedData for resumeId={}", resumeId);
            resume.setStatus(ResumeStatus.DRAFT);
            resume.setParseStatus(ResumeParseStatus.FAIL);
            resumeRepository.save(resume);
            return;
        }

        Map<String, SkillCategory> categoryCache = new HashMap<>();
        Map<String, Skill> skillCache = new HashMap<>();

        try {
            resume.setParseStatus(ResumeParseStatus.PARTIAL);
            resumeRepository.save(resume);

            clearExistingParsedData(resumeId);
            applyResumeFields(resume, parsedData.path("resume"), parsedData.path("metadata"));
            persistResumeSkills(resume, parsedData.path("resumeSkills"), categoryCache, skillCache);
            persistResumeEducations(resume, parsedData.path("resumeEducations"));
            persistResumeExperiences(resume, parsedData.path("resumeExperiences"), categoryCache, skillCache);
            persistResumeProjects(resume, parsedData.path("resumeProjects"), categoryCache, skillCache);
            persistResumeCertifications(resume, parsedData.path("resumeCertifications"));

            resume.setStatus(ResumeStatus.ACTIVE);
            resume.setParseStatus(ResumeParseStatus.FINISH);
            resumeRepository.save(resume);

            log.info("Applied resume parsing result successfully for resumeId={}", resumeId);
        } catch (Exception e) {
            resume.setStatus(ResumeStatus.DRAFT);
            resume.setParseStatus(ResumeParseStatus.FAIL);
            resumeRepository.save(resume);
            throw e;
        }
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

    private void applyResumeFields(Resume resume, JsonNode resumeNode, JsonNode metadataNode) {
        if (resumeNode == null || resumeNode.isMissingNode() || resumeNode.isNull()) {
            return;
        }

        setIfPresentText(resume::setResumeName, text(resumeNode, "resumeName"));
        setIfPresentText(resume::setFileName, text(resumeNode, "fileName"));
        setIfPresentText(resume::setRawText, text(resumeNode, "rawText"));
        setIfPresentText(resume::setAddressInResume, text(resumeNode, "addressInResume"));
        setIfPresentText(resume::setPhoneInResume, text(resumeNode, "phoneInResume"));
        setIfPresentText(resume::setEmailInResume, text(resumeNode, "emailInResume"));
        setIfPresentText(resume::setGithubLink, text(resumeNode, "githubLink"));
        setIfPresentText(resume::setLinkedinLink, text(resumeNode, "linkedinLink"));
        setIfPresentText(resume::setPortfolioLink, text(resumeNode, "portfolioLink"));
        setIfPresentText(resume::setFullName, text(resumeNode, "fullName"));
        setIfPresentText(resume::setAvatar, text(resumeNode, "avatar"));
        setIfPresentText(resume::setResumeUrl, text(resumeNode, "resumeUrl"));

        ResumeType resumeType = parseEnum(ResumeType.class, text(resumeNode, "type"));
        if (resumeType == null) {
            Boolean isOriginal = booleanValue(resumeNode.get("isOriginal"));
            if (isOriginal != null) {
                resumeType = isOriginal ? ResumeType.ORIGINAL : ResumeType.TEMPLATE;
            }
        }
        if (resumeType == null && resume.getType() == null) {
            resumeType = ResumeType.ORIGINAL;
        }
        if (resumeType != null) {
            resume.setType(resumeType);
        }

        Integer rootResumeId = integerValue(resumeNode.get("rootResumeId"));
        if (rootResumeId != null && !rootResumeId.equals(resume.getId())) {
            resumeRepository.findById(rootResumeId).ifPresent(resume::setRootResume);
        }

        ResumeStatus resumeStatus = parseEnum(ResumeStatus.class, text(resumeNode, "status"));
        if (resumeStatus != null) {
            resume.setStatus(resumeStatus);
        }

        ResumeLanguage language = parseEnum(ResumeLanguage.class, text(resumeNode, "language"));
        if (language == null) {
            language = parseEnum(ResumeLanguage.class, text(metadataNode, "resumeLanguage"));
        }
        if (language != null) {
            resume.setLanguage(language);
        }

        Boolean isDefault = booleanValue(resumeNode.get("isDefault"));
        if (isDefault != null) {
            resume.setIsDefault(isDefault);
        }

        Boolean isDeleted = booleanValue(resumeNode.get("isDeleted"));
        if (isDeleted != null) {
            resume.setIsDeleted(isDeleted);
        }
    }

    private void persistResumeSkills(
            Resume resume,
            JsonNode groupedSkillsNode,
            Map<String, SkillCategory> categoryCache,
            Map<String, Skill> skillCache
    ) {
        if (groupedSkillsNode == null || !groupedSkillsNode.isArray()) {
            return;
        }

        Map<String, ResumeSkillGroup> resumeSkillGroupCache = new HashMap<>();

        for (int groupIndex = 0; groupIndex < groupedSkillsNode.size(); groupIndex++) {
            JsonNode groupNode = groupedSkillsNode.get(groupIndex);
            String groupName = firstNonBlank(text(groupNode, "groupName"), text(groupNode, "categoryName"), "Ungrouped");
            Integer groupOrderIndex = resolveOrderIndex(integerValue(groupNode.get("orderIndex")), groupIndex + 1);
            String groupKey = normalizeLookupKey(groupName);

            ResumeSkillGroup skillGroup = resumeSkillGroupCache.get(groupKey);
            if (skillGroup == null) {
                skillGroup = ResumeSkillGroup.builder()
                        .name(groupName)
                        .orderIndex(groupOrderIndex)
                        .resume(resume)
                        .build();
                skillGroup = resumeSkillGroupRepository.save(skillGroup);
                resumeSkillGroupCache.put(groupKey, skillGroup);
            }

            JsonNode skillsNode = groupNode.get("skills");
            if (skillsNode == null || !skillsNode.isArray()) {
                continue;
            }

            for (JsonNode skillNode : skillsNode) {
                String skillName = text(skillNode, "name");
                String description = text(skillNode, "description");
                Integer yearsOfExperience = integerValue(skillNode.get("yearsOfExperience"));
                String categoryName = firstNonBlank(text(skillNode.path("category"), "name"), groupName);
                Skill skill = upsertSkill(skillName, description, categoryName, categoryCache, skillCache);
                if (skill == null) {
                    continue;
                }

                ResumeSkill resumeSkill = ResumeSkill.builder()
                        .yearsOfExperience(yearsOfExperience)
                        .skillGroup(skillGroup)
                        .skill(skill)
                        .build();
                resumeSkillRepository.save(resumeSkill);
            }
        }
    }

    private void persistResumeEducations(Resume resume, JsonNode educationsNode) {
        if (educationsNode == null || !educationsNode.isArray()) {
            return;
        }

        for (int educationIndex = 0; educationIndex < educationsNode.size(); educationIndex++) {
            JsonNode educationNode = educationsNode.get(educationIndex);
            ResumeEducation education = ResumeEducation.builder()
                    .institution(text(educationNode, "institution"))
                    .degree(parseEnum(DegreeType.class, text(educationNode, "degree")))
                    .majorField(text(educationNode, "majorField"))
                    .gpa(doubleValue(educationNode.get("gpa")))
                    .startDate(parseDate(text(educationNode, "startDate")))
                    .endDate(parseDate(text(educationNode, "endDate")))
                    .isCurrent(booleanValue(educationNode.get("isCurrent")))
                    .orderIndex(resolveOrderIndex(integerValue(educationNode.get("orderIndex")), educationIndex + 1))
                    .resume(resume)
                    .build();
            resumeEducationRepository.save(education);
        }
    }

    private void persistResumeExperiences(
            Resume resume,
            JsonNode experiencesNode,
            Map<String, SkillCategory> categoryCache,
            Map<String, Skill> skillCache
    ) {
        if (experiencesNode == null || !experiencesNode.isArray()) {
            return;
        }

        for (int experienceIndex = 0; experienceIndex < experiencesNode.size(); experienceIndex++) {
            JsonNode experienceNode = experiencesNode.get(experienceIndex);
            ResumeExperience experience = ResumeExperience.builder()
                    .company(text(experienceNode, "company"))
                    .startDate(parseDate(text(experienceNode, "startDate")))
                    .endDate(parseDate(text(experienceNode, "endDate")))
                    .isCurrent(booleanValue(experienceNode.get("isCurrent")))
                    .workingModel(parseEnum(WorkingModel.class, text(experienceNode, "workingModel")))
                    .employmentType(parseEnum(EmploymentType.class, text(experienceNode, "employmentType")))
                    .orderIndex(resolveOrderIndex(integerValue(experienceNode.get("orderIndex")), experienceIndex + 1))
                    .resume(resume)
                    .build();
            experience = resumeExperienceRepository.save(experience);

            JsonNode detailsNode = experienceNode.get("details");
            if (detailsNode == null || !detailsNode.isArray()) {
                continue;
            }

            for (int detailIndex = 0; detailIndex < detailsNode.size(); detailIndex++) {
                JsonNode detailNode = detailsNode.get(detailIndex);
                ResumeExperienceDetail detail = ResumeExperienceDetail.builder()
                        .description(text(detailNode, "description"))
                        .title(firstNonBlank(text(detailNode, "title"), text(detailNode, "position")))
                        .startDate(parseDate(text(detailNode, "startDate")))
                        .endDate(parseDate(text(detailNode, "endDate")))
                        .isCurrent(booleanValue(detailNode.get("isCurrent")))
                        .orderIndex(resolveOrderIndex(integerValue(detailNode.get("orderIndex")), detailIndex + 1))
                        .experience(experience)
                        .build();
                detail = resumeExperienceDetailRepository.save(detail);

                JsonNode detailSkillsNode = detailNode.get("skills");
                if (detailSkillsNode == null || !detailSkillsNode.isArray()) {
                    continue;
                }

                for (JsonNode detailSkillNode : detailSkillsNode) {
                    JsonNode skillNode = detailSkillNode.path("skill");
                    String skillName = text(skillNode, "name");
                    String skillDescription = text(skillNode, "description");
                    String categoryName = text(skillNode.path("category"), "name");

                    Skill skill = upsertSkill(
                            skillName,
                            skillDescription,
                            categoryName,
                            categoryCache,
                            skillCache
                    );
                    if (skill == null) {
                        continue;
                    }

                    ExperienceSkill experienceSkill = ExperienceSkill.builder()
                            .description(text(detailSkillNode, "description"))
                            .detail(detail)
                            .skill(skill)
                            .build();
                    experienceSkillRepository.save(experienceSkill);
                }
            }
        }
    }

    private void persistResumeProjects(
            Resume resume,
            JsonNode projectsNode,
            Map<String, SkillCategory> categoryCache,
            Map<String, Skill> skillCache
    ) {
        if (projectsNode == null || !projectsNode.isArray()) {
            return;
        }

        for (int projectIndex = 0; projectIndex < projectsNode.size(); projectIndex++) {
            JsonNode projectNode = projectsNode.get(projectIndex);
            ResumeProject project = ResumeProject.builder()
                    .title(text(projectNode, "title"))
                    .teamSize(integerValue(projectNode.get("teamSize")))
                    .position(text(projectNode, "position"))
                    .description(text(projectNode, "description"))
                    .projectType(parseEnum(ProjectType.class, text(projectNode, "projectType")))
                    .startDate(parseDate(text(projectNode, "startDate")))
                    .endDate(parseDate(text(projectNode, "endDate")))
                    .isCurrent(booleanValue(projectNode.get("isCurrent")))
                    .projectUrl(text(projectNode, "projectUrl"))
                    .orderIndex(resolveOrderIndex(integerValue(projectNode.get("orderIndex")), projectIndex + 1))
                    .resume(resume)
                    .build();
            project = resumeProjectRepository.save(project);

            JsonNode projectSkillsNode = projectNode.get("skills");
            if (projectSkillsNode == null || !projectSkillsNode.isArray()) {
                continue;
            }

            for (JsonNode projectSkillNode : projectSkillsNode) {
                JsonNode skillNode = projectSkillNode.path("skill");
                String skillName = text(skillNode, "name");
                String skillDescription = text(skillNode, "description");
                String categoryName = text(skillNode.path("category"), "name");

                Skill skill = upsertSkill(
                        skillName,
                        skillDescription,
                        categoryName,
                        categoryCache,
                        skillCache
                );
                if (skill == null) {
                    continue;
                }

                ProjectSkill entity = ProjectSkill.builder()
                        .description(text(projectSkillNode, "description"))
                        .project(project)
                        .skill(skill)
                        .build();
                projectSkillRepository.save(entity);
            }
        }
    }

    private void persistResumeCertifications(Resume resume, JsonNode certificationsNode) {
        if (certificationsNode == null || !certificationsNode.isArray()) {
            return;
        }

        for (JsonNode certificationNode : certificationsNode) {
            ResumeCertification certification = ResumeCertification.builder()
                    .name(text(certificationNode, "name"))
                    .issuer(text(certificationNode, "issuer"))
                    .credentialUrl(text(certificationNode, "credentialUrl"))
                    .image(text(certificationNode, "image"))
                    .description(text(certificationNode, "description"))
                    .resume(resume)
                    .build();
            resumeCertificationRepository.save(certification);
        }
    }

    private Skill upsertSkill(
            String rawSkillName,
            String skillDescription,
            String rawCategoryName,
            Map<String, SkillCategory> categoryCache,
            Map<String, Skill> skillCache
    ) {
        String skillName = normalizeFreeText(rawSkillName);
        if (skillName == null) {
            return null;
        }

        SkillCategory category = findExistingSkillCategory(rawCategoryName, categoryCache);
        String key = normalizeLookupKey(skillName);

        Skill cached = skillCache.get(key);
        if (cached != null) {
            return cached;
        }

        Object lock = skillUpsertLocks.computeIfAbsent(key, ignored -> new Object());
        synchronized (lock) {
            cached = skillCache.get(key);
            if (cached != null) {
                return cached;
            }

            List<Skill> existingSkills = skillRepository.findAllByNormalizedName(skillName);
            Skill skill;
            if (!existingSkills.isEmpty()) {
                skill = existingSkills.get(0);
                if (existingSkills.size() > 1) {
                    log.warn(
                            "Detected duplicate skills for normalized key '{}', using skillId={}",
                            key,
                            skill.getId()
                    );
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

            // Keep one global skill per normalized name.
            // Category is read-only from existing categories. Assign only if missing.
            if (category != null && skill.getCategory() == null) {
                skill.setCategory(category);
            }

            if ((skill.getDescription() == null || skill.getDescription().isBlank())
                    && skillDescription != null
                    && !skillDescription.isBlank()) {
                skill.setDescription(skillDescription);
            }
            skill = skillRepository.save(skill);

            skillCache.put(key, skill);
            return skill;
        }
    }

    private SkillCategory findExistingSkillCategory(String rawCategoryName, Map<String, SkillCategory> categoryCache) {
        String categoryName = normalizeFreeText(rawCategoryName);
        if (categoryName == null) {
            return null;
        }

        String key = normalizeLookupKey(categoryName);
        if (categoryCache.containsKey(key)) {
            return categoryCache.get(key);
        }

        List<SkillCategory> existingCategories = skillCategoryRepository.findAllByNormalizedName(categoryName);
        SkillCategory category = existingCategories.isEmpty() ? null : existingCategories.get(0);
        if (existingCategories.size() > 1) {
            log.warn(
                    "Detected duplicate skill categories for normalized name '{}', using categoryId={}",
                    categoryName,
                    category != null ? category.getId() : null
            );
        }

        categoryCache.put(key, category);
        return category;
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

    private String text(JsonNode node, String fieldName) {
        if (node == null || node.isNull() || fieldName == null) {
            return null;
        }

        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }

        String value = valueNode.asText();
        if (value == null) {
            return null;
        }

        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }

        try {
            if (rawDate.length() == 10) {
                return LocalDate.parse(rawDate);
            }
            if (rawDate.length() == 7) {
                YearMonth yearMonth = YearMonth.parse(rawDate);
                return yearMonth.atDay(1);
            }
            if (rawDate.length() == 4) {
                int year = Integer.parseInt(rawDate);
                return LocalDate.of(year, 1, 1);
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            log.debug("Unable to parse date '{}': {}", rawDate, e.getMessage());
        }
        return null;
    }

    private Double doubleValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asDouble();
        }
        if (node.isTextual()) {
            try {
                return Double.parseDouble(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Integer integerValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isInt() || node.isLong()) {
            return node.asInt();
        }
        if (node.isTextual()) {
            try {
                return Integer.parseInt(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Boolean booleanValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isTextual()) {
            String value = node.asText().trim().toLowerCase(Locale.ROOT);
            if ("true".equals(value) || "1".equals(value) || "yes".equals(value)) {
                return Boolean.TRUE;
            }
            if ("false".equals(value) || "0".equals(value) || "no".equals(value)) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumType, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String value = rawValue.trim().toUpperCase(Locale.ROOT).replace("-", "_").replace(" ", "_");
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void setIfPresentText(java.util.function.Consumer<String> setter, String value) {
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }
}
