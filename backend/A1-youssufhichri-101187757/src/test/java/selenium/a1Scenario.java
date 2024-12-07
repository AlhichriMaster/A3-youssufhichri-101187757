package selenium;

import org.example.Main;
import org.example.model.Card;
import org.example.model.Game;
import org.example.model.Player;
import org.example.service.DeckService;
import org.example.service.GameService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import selenium.config.TestConfig;

import java.time.Duration;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {Main.class, TestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@ActiveProfiles({"test", "a1-scenario"})
public class a1Scenario {
    private WebDriver driver;
    private WebDriverWait wait;

    @Autowired
    private GameService gameService;

    @Autowired
    private DeckService deckService;

    @Autowired
    private Game game;

    @Before
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();

        deckService.setTestMode(true);
        game.setEventDeck(deckService.createEventDeck());

        driver.get("///G:/School/FourthYear/COMP 4004/A3-youssufhichri-101187757/frontend/startgame.html");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("draw-button")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("player-stats-container")));
    }

    private void waitAndClick(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    private void refreshGameState() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "fetch('http://localhost:8080/api/game')" +
                            ".then(response => response.json())" +
                            ".then(data => { window.gameState = data; updatePlayerStats(); });"
            );
            waitForStateUpdate();
        } catch (Exception e) {
            System.out.println("Error refreshing game state: " + e.getMessage());
        }
    }

    private void handleHandTrim(String... cardsToDiscard) {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[contains(@class, 'hand-trimming')]")));

        for (String cardId : cardsToDiscard) {
            waitAndClick(By.xpath(
                    "//div[contains(@class, 'card')]//h4[text()='" + cardId + "']/parent::div"));
        }
        waitAndClick(By.xpath("//button[text()='Confirm Discard']"));
        waitForStateUpdate();
        refreshGameState();
    }

    private void sponsorQuest(String... cards) {
        for (String card : cards) {
            waitAndClick(By.xpath(
                    "//div[contains(@class, 'card')]//h4[text()='" + card + "']/parent::div"));
        }
        waitAndClick(By.xpath("//button[text()='Confirm Stage']"));
        waitForStateUpdate();
        refreshGameState();
    }

    private void performAttack(String... weaponCards) {
        for (String weaponCard : weaponCards) {
            waitAndClick(By.xpath(
                    "//div[contains(@class, 'card')]//h4[text()='" + weaponCard + "']/parent::div"));
        }
        waitAndClick(By.xpath("//button[text()='Confirm Attack']"));
        waitForStateUpdate();
        refreshGameState();
    }

    private void verifyPlayerStats(String playerId, int expectedShields, int expectedHandSize) {
        refreshGameState();
        String statsXPath = String.format(
                "//div[contains(@class, 'player-stat-card')]//strong[contains(text(), 'Player %s')]/ancestor::div[@class='player-stat-card' or contains(@class, 'player-stat-card')]",
                playerId
        );
        WebElement playerStats = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(statsXPath)));

        String shieldText = playerStats.findElement(By.xpath(
                ".//div[contains(@class, 'stat-row')][contains(., 'Shields')]//span[last()]"
        )).getText();
        assertEquals("Shield count mismatch for Player " + playerId,
                String.valueOf(expectedShields), shieldText.trim());

        String handText = playerStats.findElement(By.xpath(
                ".//div[contains(@class, 'stat-row')][contains(., 'Hand Size')]//span[last()]"
        )).getText();
        assertTrue("Hand size mismatch for Player " + playerId,
                handText.contains(expectedHandSize + " cards"));
    }

    private void verifyPlayerHand(String playerId, String... expectedCards) {
        Player player = game.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow();

        List<String> actualCardIds = player.getHand().stream()
                .map(Card::getId)
                .toList();

        assertEquals("Hand content mismatch for Player " + playerId,
                List.of(expectedCards), actualCardIds);
    }

    private void waitForStateUpdate() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testA1Scenario() {
        // Initial state verification
        verifyPlayerStats("P1", 0, 12);
        verifyPlayerStats("P2", 0, 12);
        verifyPlayerStats("P3", 0, 12);
        verifyPlayerStats("P4", 0, 12);

        // P1 draws quest and declines
        waitAndClick(By.id("draw-button"));
        waitForStateUpdate();
        waitAndClick(By.xpath("//button[text()='Decline']"));
        waitForStateUpdate();

        // P2 sponsors quest
        waitAndClick(By.xpath("//button[text()='Accept']"));
        waitForStateUpdate();

        // P2 builds stages
        sponsorQuest("F5", "H10");          // Stage 1
        sponsorQuest("F15", "S10");         // Stage 2
        sponsorQuest("F15", "D5", "B15");   // Stage 3
        sponsorQuest("F40", "B15");         // Stage 4


        // Stage 1 participation
        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P3
        handleHandTrim("F5");
        performAttack("S10", "D5");

        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P4
        handleHandTrim("F5");
        performAttack("D5", "H10");

        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P1
        handleHandTrim("F5");
        performAttack("D5", "S10");


        waitForStateUpdate();


        // Stage 2 participation
        performAttack("B15", "S10");//P3
        performAttack("H10", "B15");//P4
        performAttack("H10", "S10");//P1


        refreshGameState();
        verifyPlayerStats("P1", 0, 9);
        verifyPlayerHand("P1", "F5", "F10", "F15", "F15", "F30", "H10", "B15", "B15", "L20");

        // Stage 3
        performAttack("L20", "H10", "S10"); // P3
        performAttack("B15", "S10", "L20"); // P4

        // Stage 4
        performAttack("B15", "H10", "L20"); // P3
        performAttack("D5", "S10", "L20", "E30"); // P4

        // P2 discards and draws
        handleHandTrim("F5", "F5", "F5", "F5");


        refreshGameState();
        verifyPlayerStats("P2", 0, 12);
        verifyPlayerStats("P3", 0, 5);
        verifyPlayerStats("P4", 4, 4);

        verifyPlayerHand("P3", "F5", "F5", "F15", "F30", "S10");
        verifyPlayerHand("P4", "F15", "F15", "F40", "L20");

    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}