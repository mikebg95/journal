package dev.michaelgoldman.journalbackend.application.port.in;

public record CreateEntryCommand(String title, String content) {}
