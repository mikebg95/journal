package dev.michaelgoldman.journalbackend.application.port.out;

import dev.michaelgoldman.journalbackend.domain.model.Entry;
import java.util.List;

public record EntryPage(List<Entry> entries, int pageNumber, int pageSize, long totalElements) {
    public EntryPage {
        entries = List.copyOf(entries);
    }
}
