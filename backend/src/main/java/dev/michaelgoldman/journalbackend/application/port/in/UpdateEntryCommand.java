package dev.michaelgoldman.journalbackend.application.port.in;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record UpdateEntryCommand(
        long id,
        long version,
        String title,
        String content,
        @Nullable String summary,
        @Nullable String mood,
        List<String> tags,
        List<String> todos) {}
