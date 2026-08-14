package dev.michaelgoldman.journalbackend.application.port.out;

import dev.michaelgoldman.journalbackend.domain.exception.EntryVersionConflictException;
import dev.michaelgoldman.journalbackend.domain.model.Entry;
import java.util.Optional;

public interface EntryStore {
    // argument entry contains title & content; return entry also contains id, version and timestamps
    Entry create(Entry entry);

    Entry update(Entry entry) throws EntryVersionConflictException;

    Optional<Entry> findById(long id);

    void deleteById(long id);

    EntryPage findPage(EntryPageQuery query);
}
