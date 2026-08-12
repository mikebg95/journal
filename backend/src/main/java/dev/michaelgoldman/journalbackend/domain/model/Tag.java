package dev.michaelgoldman.journalbackend.domain.model;

import org.jspecify.annotations.Nullable;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public record Tag(String value) {
    private static final int CHAR_LIMIT = 50;
    private static final Pattern WHITESPACE_RUN = Pattern.compile("\\s+");

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
        return WHITESPACE_RUN.matcher(
                Normalizer.normalize(unclean, Normalizer.Form.NFC).strip())
                        .replaceAll(" ")
                        .toLowerCase(Locale.ROOT);
    }
}