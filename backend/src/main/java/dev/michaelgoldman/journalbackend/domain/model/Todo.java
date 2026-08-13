package dev.michaelgoldman.journalbackend.domain.model;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record Todo(String text) {
    private static final int CHAR_LIMIT = 1000;

    public Todo {
        if (text == null || !text.equals(TextNormaliser.toSingleLine(text)) || text.isBlank() || text.length() > CHAR_LIMIT) {
            throw new IllegalArgumentException("Todo text is not canonical");
        }
    }

    public static Optional<Todo> of(@Nullable String unclean) {
        if (unclean == null) return Optional.empty();
        String cleaned = TextNormaliser.toSingleLine(unclean);
        if (cleaned.isBlank() || cleaned.length() > CHAR_LIMIT) return Optional.empty();

        return Optional.of(new Todo(cleaned));
    }
}
