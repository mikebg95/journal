package dev.michaelgoldman.journalbackend.domain.model;

import java.text.Normalizer;
import java.util.Objects;

public final class Entry {
    private static final int TITLE_CHAR_LIMIT = 100;
    private static final int CONTENT_CHAR_LIMIT = 20000;

    private final String title;
    private final String content;

    public Entry(String title, String content) {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(content, "content");

        if (!title.equals(clean(title)) || title.isBlank() || title.length() > TITLE_CHAR_LIMIT) {
            throw new IllegalArgumentException("Entry title is not canonical");
        }

        if (!content.equals(clean(content)) || content.isBlank() || content.length() > CONTENT_CHAR_LIMIT) {
            throw new IllegalArgumentException("Entry content is not canonical");
        }

        this.title = title;
        this.content = content;
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

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
