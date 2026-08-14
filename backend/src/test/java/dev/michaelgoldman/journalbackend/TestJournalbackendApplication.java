package dev.michaelgoldman.journalbackend;

import org.springframework.boot.SpringApplication;

public class TestJournalbackendApplication {

    static void main(String[] args) {
        SpringApplication.from(JournalbackendApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
