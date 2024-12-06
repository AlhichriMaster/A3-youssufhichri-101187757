package selenium.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.example.service.DeckService;

@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public DeckService deckService() {
        DeckService service = new DeckService();
        service.setTestMode(true);
        return service;
    }
}