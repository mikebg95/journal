package dev.michaelgoldman.journalbackend;

import dev.michaelgoldman.journalbackend.adapter.out.ai.ChatClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ChatClientConfig.class)
public class JournalbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JournalbackendApplication.class, args);
	}
}
