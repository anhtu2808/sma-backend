package com.sma.core.service.impl;

import com.sma.core.entity.Job;
import com.sma.core.repository.BannedKeywordRepository;
import com.sma.core.service.BannedKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BannedKeywordServiceImpl implements BannedKeywordService {
    private final BannedKeywordRepository keywordRepository;

    public boolean isContentViolated(Job job) {
        List<String> bannedWords = keywordRepository.findByIsActiveTrue()
                .stream()
                .map(k -> Pattern.quote(k.getWord().toLowerCase()))
                .toList();

        if (bannedWords.isEmpty()) return false;
        String rawContent = buildFullContent(job);
        String normalizedContent = normalizeText(rawContent);
        String combinedRegex = "\\b(" + String.join("|", bannedWords) + ")\\b";
        Pattern pattern = Pattern.compile(combinedRegex, Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE);

        return pattern.matcher(normalizedContent).find();
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        String normalized = text.toLowerCase();
        normalized = normalized.replaceAll("[.,\\-_*\\/|]", "");
        normalized = normalized.replaceAll("\\s+", " ");
        return normalized.trim();
    }

    private String buildFullContent(Job job) {
        StringBuilder sb = new StringBuilder();
        sb.append(Objects.toString(job.getName(), " ")).append(" ");
        sb.append(Objects.toString(job.getAbout(), " ")).append(" ");
        sb.append(Objects.toString(job.getResponsibilities(), " ")).append(" ");
        sb.append(Objects.toString(job.getRequirement(), " "));

        if (job.getBenefits() != null) {
            job.getBenefits().forEach(b -> sb.append(Objects.toString(b.getName(), "")).append(" "));
        }
        if (job.getQuestions() != null) {
            job.getQuestions().forEach(q -> sb.append(Objects.toString(q.getQuestion(), "")).append(" "));
        }
        return sb.toString();
    }
}
