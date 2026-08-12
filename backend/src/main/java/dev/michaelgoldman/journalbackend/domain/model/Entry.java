package dev.michaelgoldman.journalbackend.domain.model;

import java.text.Normalizer;
import java.util.Objects;

public record Entry(String title, String content) {
    private static final int TITLE_CHAR_LIMIT = 100;
    private static final int CONTENT_CHAR_LIMIT = 20000;

    public Entry {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(content, "content");

        if (!title.equals(clean(title)) || title.isBlank() || title.length() > TITLE_CHAR_LIMIT) {
            throw new IllegalArgumentException("Entry title is not canonical");
        }

        if (!content.equals(clean(content)) || content.isBlank() || content.length() > CONTENT_CHAR_LIMIT) {
            throw new IllegalArgumentException("Entry content is not canonical");
        }
    }

    public static Entry of(String uncleanTitle, String uncleanContent) {
        Objects.requireNonNull(uncleanTitle, "title");
        Objects.requireNonNull(uncleanContent, "content");

        String cleanedTitle = clean(uncleanTitle);
        String cleanedContent = clean(uncleanContent);

        return new Entry(cleanedTitle, cleanedContent);
    }

    private static String clean(String unclean) {
        return Normalizer.normalize(unclean, Normalizer.Form.NFC).strip();
    }
}
