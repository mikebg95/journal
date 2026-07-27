package dev.michaelgoldman.notesbackend;

import dev.michaelgoldman.notesbackend.config.ChatClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ChatClientConfig.class)
public class NotesbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotesbackendApplication.class, args);
	}
}
