package dev.michaelgoldman.journalbackend.application.port.out;

import dev.michaelgoldman.journalbackend.domain.exception.EnrichmentFailedException;
import dev.michaelgoldman.journalbackend.domain.model.Enrichment;

public interface EntryEnricher {
    Enrichment enrich(String title, String content) throws EnrichmentFailedException;
}
