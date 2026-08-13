package dev.michaelgoldman.journalbackend.domain.model;

import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public enum Mood {
    HAPPY, CALM, NEUTRAL, ANXIOUS, SAD, FRUSTRATED;

    public static Optional<Mood> from(@Nullable String raw) {
        if (raw == null) {
            return Optional.empty();
        }

        String cleaned = clean(raw);

        if (cleaned.isBlank()) {
            return Optional.empty();
        }

        for (Mood mood : values()) {
            if (mood.name().equals(cleaned)) {
                return Optional.of(mood);
            }
        }

        return Optional.empty();
    }

    private static String clean(String uncleaned) {
        return uncleaned.strip().toUpperCase(Locale.ROOT);
    }
}
