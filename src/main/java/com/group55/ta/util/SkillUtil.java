package com.group55.ta.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Skill parsing and matching helper.
 */
public final class SkillUtil {
    private SkillUtil() {
    }

    public static List<String> parseSkills(String csvInput) {
        if (ValidationUtil.isBlank(csvInput)) {
            return new ArrayList<>();
        }
        Set<String> normalized = new LinkedHashSet<>();
        String[] parts = csvInput.split("[,;\\n]");
        for (String part : parts) {
            String skill = ValidationUtil.trim(part);
            if (!skill.isEmpty()) {
                normalized.add(skill);
            }
        }
        return new ArrayList<>(normalized);
    }

    public static List<String> missingSkills(List<String> candidateSkills, List<String> requiredSkills) {
        Set<String> candidate = normalize(candidateSkills);
        List<String> result = new ArrayList<>();
        if (requiredSkills == null) {
            return result;
        }
        for (String skill : requiredSkills) {
            String normalized = normalizeOne(skill);
            if (!candidate.contains(normalized)) {
                result.add(skill);
            }
        }
        return result;
    }

    public static List<String> matchedSkills(List<String> candidateSkills, List<String> requiredSkills) {
        Set<String> candidate = normalize(candidateSkills);
        List<String> result = new ArrayList<>();
        if (requiredSkills == null) {
            return result;
        }
        for (String skill : requiredSkills) {
            String normalized = normalizeOne(skill);
            if (candidate.contains(normalized)) {
                result.add(skill);
            }
        }
        return result;
    }

    public static int baseMatchScore(List<String> candidateSkills, List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return 100;
        }
        int matched = matchedSkills(candidateSkills, requiredSkills).size();
        return (int) Math.round((matched * 100.0) / requiredSkills.size());
    }

    private static Set<String> normalize(List<String> skills) {
        if (skills == null) {
            return new LinkedHashSet<>();
        }
        return skills.stream().map(SkillUtil::normalizeOne).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String normalizeOne(String skill) {
        return ValidationUtil.trim(skill).toLowerCase(Locale.ROOT);
    }
}
