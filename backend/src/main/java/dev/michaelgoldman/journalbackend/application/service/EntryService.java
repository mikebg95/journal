package dev.michaelgoldman.journalbackend.application.service;

import dev.michaelgoldman.journalbackend.application.port.in.CreateEntryCommand;
import dev.michaelgoldman.journalbackend.application.port.out.EntryEnricher;
import dev.michaelgoldman.journalbackend.application.port.out.EntryStore;
import dev.michaelgoldman.journalbackend.domain.exception.EnrichmentFailedException;
import dev.michaelgoldman.journalbackend.domain.model.Enrichment;
import dev.michaelgoldman.journalbackend.domain.model.Entry;
import java.time.Instant;

public class EntryService {
    private final EntryStore entryStore;
    private final EntryEnricher entryEnricher;

    public EntryService(EntryStore entryStore, EntryEnricher entryEnricher) {
        this.entryStore = entryStore;
        this.entryEnricher = entryEnricher;
    }

    public Entry createEntry(CreateEntryCommand command) {
        Instant createdAt = Instant.now();
        Entry created = entryStore.create(Entry.of(command.title(), command.content(), createdAt));

        try {
            Enrichment enrichment = entryEnricher.enrich(command.title(), command.content());
            return entryStore.update(created.withAnalysis(enrichment, Instant.now()));
        } catch (EnrichmentFailedException e) {
            return created;
        }
    }
}
