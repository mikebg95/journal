package dev.michaelgoldman.journalbackend.domain.model;

import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public record Tag(String value) {
    private static final int CHAR_LIMIT = 50;

    public Tag {
        if (value == null || !value.equals(clean(value)) || value.isBlank() || value.length() > CHAR_LIMIT) {
            throw new IllegalArgumentException("Tag text is not canonical");
        }
    }

    public static Optional<Tag> of(@Nullable String unclean) {
        if (unclean == null) return Optional.empty();
        String cleaned = clean(unclean);
        if (cleaned.isBlank() || cleaned.length() > CHAR_LIMIT) return Optional.empty();

        return Optional.of(new Tag(cleaned));
    }

    private static String clean(String unclean) {
        return TextNormaliser.toSingleLine(unclean).toLowerCase(Locale.ROOT);
    }
}