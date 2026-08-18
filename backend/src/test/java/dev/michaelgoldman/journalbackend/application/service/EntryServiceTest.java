package dev.michaelgoldman.journalbackend.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.michaelgoldman.journalbackend.application.port.in.CreateEntryCommand;
import dev.michaelgoldman.journalbackend.domain.model.AnalysisStatus;
import dev.michaelgoldman.journalbackend.domain.model.Enrichment;
import dev.michaelgoldman.journalbackend.domain.model.Entry;
import dev.michaelgoldman.journalbackend.testsupport.EntryEnricherFake;
import dev.michaelgoldman.journalbackend.testsupport.EntryStoreFake;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class EntryServiceTest {

    private static final String VALID_TITLE = "Valid title";
    private static final String VALID_CONTENT = "Example of some valid content.";
    private static final String VALID_SUMMARY = "Some summary";
    private static final List<String> VALID_TAGS = List.of("a", "b", "c");
    private static final List<String> VALID_TODOS = List.of("1", "2");
    private static final String VALID_MOOD = "NEUTRAL";

    private final EntryStoreFake storeFake = new EntryStoreFake();
    private final EntryEnricherFake enricherFake = new EntryEnricherFake();

    private final EntryService entryService = new EntryService(storeFake, enricherFake);

    @Test
    void createEntry_shouldReturnAnalysedEntry() {
        // Arrange
        CreateEntryCommand command = new CreateEntryCommand(VALID_TITLE, VALID_CONTENT);
        Enrichment willReturn = Enrichment.fromModel(VALID_SUMMARY, VALID_TAGS, VALID_TODOS, VALID_MOOD);
        enricherFake.willReturn(willReturn);

        // Act
        Entry entry = entryService.createEntry(command);
        Enrichment enrichment = entry.getEnrichment();

        // Assert
        assertEquals(VALID_TITLE, entry.getTitle());
        assertEquals(VALID_CONTENT, entry.getContent());
        assertEquals(willReturn, enrichment);
        assertNotNull(entry.getCreatedAt());
        assertNotNull(entry.getAnalysedAt());
    }

    @Test
    void createEntry_shouldPersistTheEnrichment() {
        // Arrange
        CreateEntryCommand command = new CreateEntryCommand(VALID_TITLE, VALID_CONTENT);
        Enrichment willReturn = Enrichment.fromModel(VALID_SUMMARY, VALID_TAGS, VALID_TODOS, VALID_MOOD);
        enricherFake.willReturn(willReturn);

        // Act
        Entry entry = entryService.createEntry(command);
        Entry stored = storedCopyOf(entry);

        // Assert
        assertEquals(willReturn, stored.getEnrichment());
    }

    @Test
    void createEntry_whenEnrichmentFails_shouldPersistEntryWithoutEnrichment() {
        // Arrange
        CreateEntryCommand command = new CreateEntryCommand(VALID_TITLE, VALID_CONTENT);
        enricherFake.willFail();

        // Act
        Entry entry = entryService.createEntry(command);
        Entry stored = storedCopyOf(entry);

        // Assert
        assertEquals(AnalysisStatus.NOT_ANALYSED, entry.getAnalysisStatus());
        assertNull(stored.getAnalysedAt());
        assertEquals(Enrichment.empty(), stored.getEnrichment());
    }

    @Test
    void createEntry_shouldEnrichWithCorrectTitleAndContent() {
        // Arrange
        CreateEntryCommand command = new CreateEntryCommand(VALID_TITLE, VALID_CONTENT);

        // Act
        entryService.createEntry(command);

        // Assert
        assertEquals(VALID_TITLE, enricherFake.lastTitle());
        assertEquals(VALID_CONTENT, enricherFake.lastContent());
    }

    private Entry storedCopyOf(Entry entry) {
        return storeFake.findById(Objects.requireNonNull(entry.getId())).orElseThrow();
    }
}
