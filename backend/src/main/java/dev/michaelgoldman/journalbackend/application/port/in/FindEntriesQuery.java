package dev.michaelgoldman.journalbackend.application.port.in;

import dev.michaelgoldman.journalbackend.domain.model.Mood;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record FindEntriesQuery(int pageNumber, @Nullable String search, List<String> tags, Set<Mood> moods) {
    public FindEntriesQuery {
        tags = List.copyOf(tags);
        moods = Set.copyOf(moods);
    }
}
