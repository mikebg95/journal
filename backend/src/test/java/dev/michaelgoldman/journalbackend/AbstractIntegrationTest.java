package dev.michaelgoldman.journalbackend;

import dev.michaelgoldman.journalbackend.adapter.in.web.WebConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WebConfig.class})
abstract class AbstractIntegrationTest {
}
