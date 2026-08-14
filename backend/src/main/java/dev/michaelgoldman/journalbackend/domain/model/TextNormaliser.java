package dev.michaelgoldman.journalbackend.domain.model;

import java.text.Normalizer;
import java.util.regex.Pattern;

final class TextNormaliser {
    private static final Pattern WHITESPACE_RUN = Pattern.compile("\\s+");

    private TextNormaliser() {}

    static String toSingleLine(String text) {
        return WHITESPACE_RUN
                .matcher(Normalizer.normalize(text, Normalizer.Form.NFC).strip())
                .replaceAll(" ");
    }
}
