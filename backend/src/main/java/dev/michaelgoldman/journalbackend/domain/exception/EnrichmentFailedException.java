package dev.michaelgoldman.journalbackend.domain.exception;

public class EnrichmentFailedException extends RuntimeException {

    public EnrichmentFailedException(String message) {
        super(message);
    }

    public EnrichmentFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
