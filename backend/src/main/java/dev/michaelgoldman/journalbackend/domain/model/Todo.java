package dev.michaelgoldman.journalbackend.domain.model;

import org.jspecify.annotations.Nullable;

import java.text.Normalizer;
import java.util.Optional;
import java.util.regex.Pattern;

public record Todo(String text) {
    private static final int CHAR_LIMIT = 1000;
    private static final Pattern WHITESPACE_RUN = Pattern.compile("\\s+");

    public Todo {
        if (text == null || !text.equals(clean(text)) || text.isBlank() || text.length() > CHAR_LIMIT) {
            throw new IllegalArgumentException("Todo text is not canonical");
        }
    }

    public static Optional<Todo> of(@Nullable String unclean) {
        if (unclean == null) return Optional.empty();
        String cleaned = clean(unclean);
        if (cleaned.isBlank() || cleaned.length() > CHAR_LIMIT) return Optional.empty();

        return Optional.of(new Todo(cleaned));
    }

    private static String clean(String unclean) {
        return WHITESPACE_RUN.matcher(
                        Normalizer.normalize(unclean, Normalizer.Form.NFC).strip())
                .replaceAll(" ");
    }
}
