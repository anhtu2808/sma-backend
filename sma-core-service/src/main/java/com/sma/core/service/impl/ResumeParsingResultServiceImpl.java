package com.sma.core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.sma.core.dto.message.resume.ResumeParsingResultMessage;
import com.sma.core.entity.*;
import com.sma.core.enums.DegreeType;
import com.sma.core.enums.ProjectType;
import com.sma.core.enums.ResumeLanguage;
import com.sma.core.enums.ResumeStatus;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
    ResumeEducationRepository resumeEducationRepository;
    ResumeExperienceRepository resumeExperienceRepository;
    ResumeExperienceDetailRepository resumeExperienceDetailRepository;
    ExperienceSkillRepository experienceSkillRepository;
    ResumeProjectRepository resumeProjectRepository;
    ProjectSkillRepository projectSkillRepository;
    ResumeCertificationRepository resumeCertificationRepository;

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

        if (!message.isSuccess()) {
            log.warn(
                    "Resume parsing failed for resumeId={}, error={}",
                    resumeId,
                    message.getErrorMessage()
            );
            resume.setStatus(ResumeStatus.DRAFT);
            resumeRepository.save(resume);
            return;
        }

        JsonNode parsedData = message.getParsedData();
        if (parsedData == null || parsedData.isNull()) {
            log.warn("Ignore success result with empty parsedData for resumeId={}", resumeId);
            resume.setStatus(ResumeStatus.DRAFT);
            resumeRepository.save(resume);
            return;
        }

        Map<String, SkillCategory> categoryCache = new HashMap<>();
        Map<String, Skill> skillCache = new HashMap<>();

        clearExistingParsedData(resumeId);
        applyResumeFields(resume, parsedData.path("resume"), parsedData.path("metadata"));
        persistResumeSkills(resume, parsedData.path("resumeSkills"), categoryCache, skillCache);
        persistResumeEducations(resume, parsedData.path("resumeEducations"));
        persistResumeExperiences(resume, parsedData.path("resumeExperiences"), categoryCache, skillCache);
        persistResumeProjects(resume, parsedData.path("resumeProjects"), categoryCache, skillCache);
        persistResumeCertifications(resume, parsedData.path("resumeCertifications"));

        resume.setStatus(ResumeStatus.ACTIVE);
        resumeRepository.save(resume);

        log.info("Applied resume parsing result successfully for resumeId={}", resumeId);
    }

    private void clearExistingParsedData(Integer resumeId) {
        projectSkillRepository.deleteByResumeId(resumeId);
        experienceSkillRepository.deleteByResumeId(resumeId);
        resumeExperienceDetailRepository.deleteByResumeId(resumeId);
        resumeProjectRepository.deleteByResumeId(resumeId);
        resumeExperienceRepository.deleteByResumeId(resumeId);
        resumeSkillRepository.deleteByResumeId(resumeId);
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

        Boolean isOriginal = booleanValue(resumeNode.get("isOriginal"));
        if (isOriginal != null) {
            resume.setIsOriginal(isOriginal);
        }

        ResumeStatus resumeStatus = parseEnum(ResumeStatus.class, text(resumeNode, "status"));
        if (resumeStatus != null) {
            resume.setStatus(resumeStatus);
        }

        ResumeLanguage language = parseEnum(ResumeLanguage.class, text(resumeNode, "language"));
        if (language == null) {
            language = parseEnum(ResumeLanguage.class, text(metadataNode, "cvLanguage"));
        }
        if (language != null) {
            resume.setLanguage(language);
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

        for (JsonNode groupNode : groupedSkillsNode) {
            String rawCategoryName = text(groupNode, "categoryName");
            String rawSkillSection = text(groupNode, "rawSkillSection");
            JsonNode skillsNode = groupNode.get("skills");
            if (skillsNode == null || !skillsNode.isArray()) {
                continue;
            }

            for (JsonNode skillNode : skillsNode) {
                String skillName = text(skillNode, "name");
                String description = text(skillNode, "description");
                Skill skill = upsertSkill(skillName, description, rawCategoryName, categoryCache, skillCache);
                if (skill == null) {
                    continue;
                }

                ResumeSkill resumeSkill = ResumeSkill.builder()
                        .rawSkillSection(rawSkillSection)
                        .skill(skill)
                        .resume(resume)
                        .build();
                resumeSkillRepository.save(resumeSkill);
            }
        }
    }

    private void persistResumeEducations(Resume resume, JsonNode educationsNode) {
        if (educationsNode == null || !educationsNode.isArray()) {
            return;
        }

        for (JsonNode educationNode : educationsNode) {
            ResumeEducation education = ResumeEducation.builder()
                    .institution(text(educationNode, "institution"))
                    .degree(parseEnum(DegreeType.class, text(educationNode, "degree")))
                    .majorField(text(educationNode, "majorField"))
                    .gpa(doubleValue(educationNode.get("gpa")))
                    .startDate(parseDate(text(educationNode, "startDate")))
                    .endDate(parseDate(text(educationNode, "endDate")))
                    .isCurrent(booleanValue(educationNode.get("isCurrent")))
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

        for (JsonNode experienceNode : experiencesNode) {
            ResumeExperience experience = ResumeExperience.builder()
                    .company(text(experienceNode, "company"))
                    .startDate(parseDate(text(experienceNode, "startDate")))
                    .endDate(parseDate(text(experienceNode, "endDate")))
                    .isCurrent(booleanValue(experienceNode.get("isCurrent")))
                    .resume(resume)
                    .build();
            experience = resumeExperienceRepository.save(experience);

            JsonNode detailsNode = experienceNode.get("details");
            if (detailsNode == null || !detailsNode.isArray()) {
                continue;
            }

            for (JsonNode detailNode : detailsNode) {
                ResumeExperienceDetail detail = ResumeExperienceDetail.builder()
                        .description(text(detailNode, "description"))
                        .title(text(detailNode, "title"))
                        .position(text(detailNode, "position"))
                        .startDate(parseDate(text(detailNode, "startDate")))
                        .endDate(parseDate(text(detailNode, "endDate")))
                        .isCurrent(booleanValue(detailNode.get("isCurrent")))
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

        for (JsonNode projectNode : projectsNode) {
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
        if (rawSkillName == null || rawSkillName.isBlank()) {
            return null;
        }

        String skillName = rawSkillName.trim();
        SkillCategory category = upsertSkillCategory(rawCategoryName, categoryCache);
        String key = skillName.toLowerCase(Locale.ROOT) + "::" + category.getId();

        Skill cached = skillCache.get(key);
        if (cached != null) {
            return cached;
        }

        Optional<Skill> foundSkill = skillRepository.findByNameIgnoreCaseAndCategory_Id(skillName, category.getId());
        Skill skill = foundSkill.orElseGet(() -> skillRepository.save(
                Skill.builder()
                        .name(skillName)
                        .description(skillDescription)
                        .category(category)
                        .build()
        ));

        if (foundSkill.isPresent()
                && (skill.getDescription() == null || skill.getDescription().isBlank())
                && skillDescription != null
                && !skillDescription.isBlank()) {
            skill.setDescription(skillDescription);
            skill = skillRepository.save(skill);
        }

        skillCache.put(key, skill);
        return skill;
    }

    private SkillCategory upsertSkillCategory(String rawCategoryName, Map<String, SkillCategory> categoryCache) {
        String categoryName = normalizeCategoryName(rawCategoryName);
        String key = categoryName.toLowerCase(Locale.ROOT);

        SkillCategory cached = categoryCache.get(key);
        if (cached != null) {
            return cached;
        }

        SkillCategory category = skillCategoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> skillCategoryRepository.save(
                        SkillCategory.builder()
                                .name(categoryName)
                                .build()
                ));

        categoryCache.put(key, category);
        return category;
    }

    private String normalizeCategoryName(String rawCategoryName) {
        if (rawCategoryName == null || rawCategoryName.isBlank()) {
            return "Other";
        }

        String normalized = rawCategoryName.trim().toLowerCase(Locale.ROOT);

        if (normalized.contains("programming") || normalized.contains("language")) {
            return "Programming Language";
        }
        if (normalized.contains("framework")) {
            return "Framework";
        }
        if (normalized.contains("tool")) {
            return "Tool";
        }
        if (normalized.contains("database") || normalized.contains("sql")) {
            return "Database";
        }
        if (normalized.contains("front")) {
            return "Frontend";
        }
        if (normalized.contains("back")) {
            return "Backend";
        }
        if (normalized.contains("devops") || normalized.contains("ci/cd")) {
            return "DevOps";
        }
        if (normalized.contains("soft")) {
            return "Soft Skills";
        }
        if (normalized.contains("methodolog") || normalized.contains("agile") || normalized.contains("scrum")
                || normalized.contains("sdlc")) {
            return "Methodology";
        }
        if (normalized.contains("cloud") || normalized.contains("aws") || normalized.contains("azure")
                || normalized.contains("gcp")) {
            return "Cloud";
        }

        return switch (normalized) {
            case "programming language", "framework", "tool", "database", "frontend", "backend",
                    "devops", "soft skills", "methodology", "cloud" -> toTitleCase(normalized);
            default -> "Other";
        };
    }

    private String toTitleCase(String value) {
        return switch (value) {
            case "programming language" -> "Programming Language";
            case "soft skills" -> "Soft Skills";
            default -> Character.toUpperCase(value.charAt(0)) + value.substring(1);
        };
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
