package com.group55.ta.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Extracts skill keywords from raw CV text using a curated dictionary that mirrors the
 * {@code SKILL_CATALOG} used by the recruitment business layer. Each canonical skill carries a
 * list of case-insensitive aliases so that "py" / "python3" / "python" all resolve to {@code Python}.
 * <p>
 * The matcher is intentionally conservative: it only fires on whole-word boundaries to avoid
 * accidentally picking up "java" inside "javascript" when {@code Java} is the canonical entry.
 */
public final class CvSkillExtractor {

    private static final Map<String, List<String>> DICTIONARY = buildDictionary();

    private CvSkillExtractor() {
    }

    /**
     * @param cvText raw CV plain text (already stripped from PDF/DOCX)
     * @return canonical skill names found in {@code cvText}, preserving dictionary order
     */
    public static List<String> extract(String cvText) {
        if (cvText == null || cvText.isEmpty()) {
            return Collections.emptyList();
        }
        String haystack = cvText.toLowerCase(Locale.ROOT);
        Set<String> hits = new LinkedHashSet<>();
        for (Map.Entry<String, List<String>> entry : DICTIONARY.entrySet()) {
            for (String alias : entry.getValue()) {
                if (containsWord(haystack, alias)) {
                    hits.add(entry.getKey());
                    break;
                }
            }
        }
        return new ArrayList<>(hits);
    }

    /**
     * Merges {@code extracted} into {@code existing} preserving existing order and skipping
     * case-insensitive duplicates. Returns a new list; inputs are not mutated.
     */
    public static List<String> mergeSkills(List<String> existing, List<String> extracted) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<String> merged = new ArrayList<>();
        if (existing != null) {
            for (String skill : existing) {
                if (skill == null) {
                    continue;
                }
                String key = skill.toLowerCase(Locale.ROOT);
                if (seen.add(key)) {
                    merged.add(skill);
                }
            }
        }
        if (extracted != null) {
            for (String skill : extracted) {
                if (skill == null) {
                    continue;
                }
                String key = skill.toLowerCase(Locale.ROOT);
                if (seen.add(key)) {
                    merged.add(skill);
                }
            }
        }
        return merged;
    }

    private static boolean containsWord(String haystackLowercase, String needle) {
        String token = needle.toLowerCase(Locale.ROOT);
        int from = 0;
        while (from <= haystackLowercase.length() - token.length()) {
            int idx = haystackLowercase.indexOf(token, from);
            if (idx < 0) {
                return false;
            }
            boolean leftOk = idx == 0 || !isWordChar(haystackLowercase.charAt(idx - 1));
            int afterIdx = idx + token.length();
            boolean rightOk = afterIdx == haystackLowercase.length()
                    || !isWordChar(haystackLowercase.charAt(afterIdx));
            if (leftOk && rightOk) {
                return true;
            }
            from = idx + 1;
        }
        return false;
    }

    private static boolean isWordChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '+' || ch == '#';
    }

    private static Map<String, List<String>> buildDictionary() {
        LinkedHashMap<String, List<String>> map = new LinkedHashMap<>();
        map.put("Java", Arrays.asList("java"));
        map.put("Python", Arrays.asList("python", "python3", "py"));
        map.put("Mathematics", Arrays.asList("mathematics", "calculus", "linear algebra", "discrete math"));
        map.put("Software Engineering", Arrays.asList("software engineering", "agile", "scrum"));
        map.put("Data Analysis", Arrays.asList("data analysis", "data analytics", "pandas", "numpy"));
        map.put("Communication", Arrays.asList("communication", "presentation"));
        map.put("English", Arrays.asList("english", "ielts", "toefl"));
        map.put("Marking", Arrays.asList("marking", "grading"));
        map.put("Lab Support", Arrays.asList("lab support", "laboratory"));
        map.put("Invigilation", Arrays.asList("invigilation", "proctoring"));
        map.put("Problem Solving", Arrays.asList("problem solving", "problem-solving"));
        map.put("Git", Arrays.asList("git", "github", "gitlab"));
        map.put("Database", Arrays.asList("database", "sql", "mysql", "postgres"));
        map.put("Office Hours", Arrays.asList("office hours"));
        map.put("Teaching Support", Arrays.asList("teaching support", "teaching assistant", "ta experience"));
        return map;
    }
}
