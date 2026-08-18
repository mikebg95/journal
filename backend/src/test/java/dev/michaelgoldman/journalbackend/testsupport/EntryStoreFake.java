package dev.michaelgoldman.journalbackend.testsupport;

import dev.michaelgoldman.journalbackend.application.port.in.Page;
import dev.michaelgoldman.journalbackend.application.port.out.EntryPageQuery;
import dev.michaelgoldman.journalbackend.application.port.out.EntryStore;
import dev.michaelgoldman.journalbackend.domain.model.Entry;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class EntryStoreFake implements EntryStore {
    private final Map<Long, Entry> entries = new HashMap<>();
    private long nextId = 0L;
    private static final long INITIAL_VERSION = 1L;

    @Override
    public Entry create(Entry entry) {
        long id = ++nextId;

        Entry created = copyWithIdentity(id, INITIAL_VERSION, entry);
        entries.put(id, created);

        return created;
    }

    @Override
    public Entry update(Entry entry) {
        long id = Objects.requireNonNull(entry.getId(), "cannot update an entry with no id");
        long version = Objects.requireNonNull(entry.getVersion(), "cannot update an entry with no version");

        if (!entries.containsKey(id)) {
            throw new IllegalStateException("no entry with id " + id);
        }

        Entry updated = copyWithIdentity(id, version + 1, entry);
        entries.put(id, updated);

        return updated;
    }

    @Override
    public Optional<Entry> findById(long id) {
        return Optional.ofNullable(entries.get(id));
    }

    @Override
    public boolean deleteById(long id) {
        throw new UnsupportedOperationException("not needed yet");
    }

    @Override
    public Page<Entry> findPage(EntryPageQuery query) {
        throw new UnsupportedOperationException("not needed yet");
    }

    private Entry copyWithIdentity(long id, long version, Entry entry) {
        return Entry.fromStorage(
                id,
                version,
                entry.getTitle(),
                entry.getContent(),
                entry.getEnrichment(),
                entry.getCreatedAt(),
                entry.getLastUpdated(),
                entry.getAnalysedAt());
    }
}
