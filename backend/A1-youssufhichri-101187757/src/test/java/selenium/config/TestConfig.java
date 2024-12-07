package selenium.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
//import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.example.service.DeckService;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    @Profile("two-winner-test")
    public DeckService twoWinnerDeckService() {
        DeckService service = new DeckService();
        service.setTestMode(true);
        service.setTestScenario("TWO_WINNER");
        return service;
    }

    @Bean
    @Primary
    @Profile("one-winner-test")
    public DeckService oneWinnerDeckService() {
        DeckService service = new DeckService();
        service.setTestMode(true);
        service.setTestScenario("ONE_WINNER");
        return service;
    }

    @Bean
    @Primary
    @Profile("zero-winner-test")
    public DeckService zeroWinnerDeckService() {
        DeckService service = new DeckService();
        service.setTestMode(true);
        service.setTestScenario("ZERO_WINNER");
        return service;
    }

    @Bean
    @Primary
    @Profile("a1-scenario")
    public DeckService a1ScenarioDeckService() {
        DeckService service = new DeckService();
        service.setTestMode(true);
        service.setTestScenario("A1_SCENARIO");
        return service;
    }
}