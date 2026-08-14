package dev.michaelgoldman.journalbackend.domain.exception;

public class EntryNotFoundException extends RuntimeException {

    public EntryNotFoundException(long entryId) {
        super("Entry " + entryId + " not found");
    }
}
