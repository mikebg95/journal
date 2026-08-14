package dev.michaelgoldman.journalbackend.application.port.out;

import dev.michaelgoldman.journalbackend.application.port.in.Page;
import dev.michaelgoldman.journalbackend.domain.exception.EntryVersionConflictException;
import dev.michaelgoldman.journalbackend.domain.model.Entry;
import java.util.Optional;

public interface EntryStore {
    /**
     * @param entry a new entry carrying title and content, with no id or version
     * @return the stored entry, now carrying id, version and timestamps
     */
    Entry create(Entry entry);

    /**
     * @throws EntryVersionConflictException if the stored version no longer matches the entry's version
     */
    Entry update(Entry entry);

    Optional<Entry> findById(long id);

    /**
     * @return true if a row was deleted, false if no entry with this id existed
     */
    boolean deleteById(long id);

    Page<Entry> findPage(EntryPageQuery query);
}
