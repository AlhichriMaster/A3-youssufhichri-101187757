package selenium.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.example.service.DeckService;

@TestConfiguration
public class TestConfig {

    // Remove the default configuration bean and create separate ones for each scenario
    @Bean
    @Primary
    @org.springframework.context.annotation.Profile("two-winner-test")
    public DeckService twoWinnerDeckService() {
        DeckService service = new DeckService();
        service.setTestMode(true);
        service.setTestScenario("TWO_WINNER");
        return service;
    }

    @Bean
    @Primary
    @org.springframework.context.annotation.Profile("one-winner-test")
    public DeckService oneWinnerDeckService() {
        DeckService service = new DeckService();
        service.setTestMode(true);
        service.setTestScenario("ONE_WINNER");
        return service;
    }

    @Bean
    @Primary
    @org.springframework.context.annotation.Profile("zero-winner-test")
    public DeckService zeroWinnerScenario() {
        DeckService service = new DeckService();
        service.setTestMode(true);
        service.setTestScenario("ZERO_WINNER");
        return service;
    }
}