package dev.michaelgoldman.journalbackend.application.port.out;

import dev.michaelgoldman.journalbackend.domain.exception.EnrichmentFailedException;
import dev.michaelgoldman.journalbackend.domain.model.Enrichment;

public interface EntryEnricher {
    /**
     * @throws EnrichmentFailedException if the model call fails, times out, or returns unusable output
     */
    Enrichment enrich(String title, String content);
}
