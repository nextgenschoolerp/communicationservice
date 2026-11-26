package com.kvs.communication.service;

import com.kvs.core.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Phase 6 (i18n): the versions an author types are checked and stored as JSON; a stored value that cannot be read gives none. */
class CircularTranslationsTest {

    private static Map<String, String> version(String title, String body) {
        return Map.of("title", title, "body", body);
    }

    @Test
    void languageCodesAreLowercasedAndTrimmed() {
        var out = CircularTranslations.validate(Map.of("HI", version("  सूचना  ", " नमस्ते ")));

        assertThat(out).containsOnlyKeys("hi");
        assertThat(out.get("hi")).isEqualTo(new CircularTranslations.Version("सूचना", "नमस्ते"));
    }

    @Test
    void aVersionNeedsATitleAndAMessage() {
        assertThatThrownBy(() -> CircularTranslations.validate(Map.of("hi", version("", "नमस्ते"))))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("title and a message");
        assertThatThrownBy(() -> CircularTranslations.validate(Map.of("hi", Map.of("title", "सूचना"))))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("title and a message");
    }

    @Test
    void theTitleFitsTheColumn() {
        String longTitle = "x".repeat(201);

        assertThatThrownBy(() -> CircularTranslations.validate(Map.of("hi", version(longTitle, "नमस्ते"))))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("200 characters");
    }

    @Test
    void aLanguageCodeIsCheckedAndNotGivenTwice() {
        assertThatThrownBy(() -> CircularTranslations.validate(Map.of("hindi!", version("a", "b"))))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("not valid");
        assertThatThrownBy(() -> CircularTranslations.validate(Map.of("hi", version("a", "b"), "HI", version("c", "d"))))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("given twice");
    }

    @Test
    void noVersionsMeansNoJson() {
        assertThat(CircularTranslations.validate(null)).isEmpty();
        assertThat(CircularTranslations.toJson(CircularTranslations.validate(Map.of()))).isNull();
    }

    @Test
    void theJsonRoundTrips() {
        String json = CircularTranslations.toJson(CircularTranslations.validate(Map.of("hi", version("सूचना", "नमस्ते"))));

        assertThat(CircularTranslations.fromJson(json)).containsEntry("hi", new CircularTranslations.Version("सूचना", "नमस्ते"));
    }

    @Test
    void anEmptyOrUnreadableStoredValueGivesNoVersions() {
        assertThat(CircularTranslations.fromJson(null)).isEmpty();
        assertThat(CircularTranslations.fromJson(" ")).isEmpty();
        assertThat(CircularTranslations.fromJson("{not json")).isEmpty();
    }
}
