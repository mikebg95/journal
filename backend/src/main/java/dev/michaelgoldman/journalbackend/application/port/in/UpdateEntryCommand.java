package dev.michaelgoldman.journalbackend.application.port.in;

import dev.michaelgoldman.journalbackend.domain.model.Mood;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record UpdateEntryCommand(
        long id,
        long version,
        String title,
        String content,
        @Nullable String summary,
        @Nullable Mood mood,
        List<String> tags,
        List<String> todos) {
    public UpdateEntryCommand {
        tags = List.copyOf(tags);
        todos = List.copyOf(todos);
    }
}
