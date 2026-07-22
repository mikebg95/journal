package dev.michaelgoldman.notesbackend;

import org.springframework.boot.SpringApplication;

public class TestNotesbackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(NotesbackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
