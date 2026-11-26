package com.kvs.communication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvs.core.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * A circular's translated versions (Phase 6 of docs/plans/i18n-plan.md). The author types them; they are stored on the
 * circular as JSON: {"hi": {"title": "...", "body": "..."}}. A recipient whose language has no version gets the main text.
 */
@Slf4j
public final class CircularTranslations {

    /** One version: a title and a message in one language */
    public record Version(String title, String body) { }

    /** Column size of the title (communication_broadcasts.title) */
    static final int MAX_TITLE = 200;
    static final int MAX_LANGUAGES = 10;

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, Version>> TYPE = new TypeReference<>() { };

    private CircularTranslations() { }

    /**
     * Checks and cleans the versions the author sent: language codes lowercase (en, hi, hi-IN), each version with a title and
     * a message, the title within the column size, no language twice, at most {@value #MAX_LANGUAGES} versions.
     */
    public static Map<String, Version> validate(Map<String, Map<String, String>> raw) {
        Map<String, Version> out = new TreeMap<>();
        if (raw == null || raw.isEmpty()) return out;
        if (raw.size() > MAX_LANGUAGES) throw new BadRequestException("A circular can have at most " + MAX_LANGUAGES + " translations");
        for (Map.Entry<String, Map<String, String>> e : raw.entrySet()) {
            String lang = e.getKey() == null ? "" : e.getKey().trim().toLowerCase(Locale.ROOT);
            if (!lang.matches("[a-z]{2,3}(-[a-z]{2,4})?")) throw new BadRequestException("Language code \"" + e.getKey() + "\" is not valid");
            if (out.containsKey(lang)) throw new BadRequestException("Language " + lang + " is given twice");
            String title = e.getValue() == null ? null : e.getValue().get("title");
            String body = e.getValue() == null ? null : e.getValue().get("body");
            if (blank(title) || blank(body)) throw new BadRequestException("The " + lang + " version needs both a title and a message");
            if (title.trim().length() > MAX_TITLE) throw new BadRequestException("The " + lang + " title is longer than " + MAX_TITLE + " characters");
            out.put(lang, new Version(title.trim(), body.trim()));
        }
        return out;
    }

    /** The JSON to store, or null when there are no versions */
    public static String toJson(Map<String, Version> versions) {
        if (versions == null || versions.isEmpty()) return null;
        try {
            return JSON.writeValueAsString(versions);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not store the translations", e);
        }
    }

    /** The versions stored on a circular; none when the column is empty or cannot be read (the main text is then sent) */
    public static Map<String, Version> fromJson(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return JSON.readValue(json, TYPE);
        } catch (JsonProcessingException e) {
            log.warn("Stored circular translations cannot be read, the main text is sent: {}", e.getOriginalMessage());
            return Map.of();
        }
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
