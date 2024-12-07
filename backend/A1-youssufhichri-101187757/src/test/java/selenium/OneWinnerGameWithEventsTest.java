package selenium;

import org.example.Main;
import org.example.model.Card;
import org.example.model.Player;
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
import org.example.service.DeckService;
import org.example.service.GameService;
import org.example.model.Game;
import selenium.config.TestConfig;

import java.time.Duration;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {Main.class, TestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@ActiveProfiles({"test", "one-winner-test"})
public class OneWinnerGameWithEventsTest {

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
//        deckService.setTestScenario("ONE_WINNER");
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

    private void waitForStateUpdate() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testOneWinnerGameWithEvents() {
        // Initial state verification
        verifyPlayerStats("P1", 0, 12);
        verifyPlayerStats("P2", 0, 12);
        verifyPlayerStats("P3", 0, 12);
        verifyPlayerStats("P4", 0, 12);

        for(Player player : game.getPlayers()){
            System.out.print(player.getId() + "'s cards: ");
            for (Card card : player.getHand()){
                System.out.print(card.getId() + ", ");
            }
            System.out.print("\n");
        }

        // First Quest
        waitAndClick(By.id("draw-button"));
        waitForStateUpdate();
        waitAndClick(By.xpath("//button[text()='Accept']"));
        waitForStateUpdate();

        // P1 sponsors quest stages
        sponsorQuest("F5");  // Stage 1
        sponsorQuest("F10");  // Stage 2
        sponsorQuest("F15");  // Stage 3
        sponsorQuest("F20");  // Stage 4

        // Stage 1 participation
        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P2
        handleHandTrim("F5");
        performAttack("S10");

        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P3
        handleHandTrim("F10");
        performAttack("S10");

        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P4
        handleHandTrim("F20");
        performAttack("S10");

        // Stage 2
        performAttack("H10"); // P2
        performAttack("H10"); // P3
        performAttack("H10"); // P4

        // Stage 3
        performAttack("B15"); // P2
        performAttack("B15"); // P3
        performAttack("B15"); // P4

        // Stage 4
        performAttack("L20"); // P2
        performAttack("L20"); // P3
        performAttack("L20"); // P4

        // P1 discards and draws
        handleHandTrim("F5", "F5", "F10", "F10");

        refreshGameState();
        verifyPlayerStats("P2", 4, 11);
        verifyPlayerStats("P3", 4, 11);
        verifyPlayerStats("P4", 4, 11);

        // P2 draws Plague
        waitAndClick(By.id("draw-button"));
        waitForStateUpdate();
        waitAndClick(By.xpath("//button[text()='Continue']"));
        waitForStateUpdate();

        verifyPlayerStats("P2", 2, 11);

        // P3 draws Prosperity
        waitAndClick(By.id("draw-button"));
        waitForStateUpdate();

        // Handle discards for each player after prosperity
        handleHandTrim("F5", "F10");    // P1
        handleHandTrim("F5");           // P2
        handleHandTrim("F5");           // P3
        handleHandTrim("F20");          // P4

        // P4 draws Queen's favor
        waitAndClick(By.id("draw-button"));
        waitForStateUpdate();
        handleHandTrim("F25", "F30");

        // Final Quest
        waitAndClick(By.id("draw-button"));
        waitForStateUpdate();
        waitAndClick(By.xpath("//button[text()='Accept']"));
        waitForStateUpdate();

        // P1 sponsors second quest stages
        sponsorQuest("F15");                // Stage 1
        sponsorQuest("F15", "D5");         // Stage 2
        sponsorQuest("F20", "D5");         // Stage 3

        // Stage 1 participation
        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P2
        handleHandTrim("F5");
        performAttack("B15");

        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P3
        handleHandTrim("F10");
        performAttack("B15");

        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P4
        handleHandTrim("F20");
        performAttack("H10");

        // Stage 2
        performAttack("B15", "H10"); // P2
        performAttack("B15", "S10"); // P3
//        performAttack(); // P4 loses

        // Stage 3
        performAttack("L20", "S10"); // P2
        performAttack("E30"); // P3

        // P1 discards and draws
//        handleHandTrim("F15", "F15", "F15");

        // Verify final state
        refreshGameState();
        verifyPlayerStats("P1", 0, 7);
        verifyPlayerStats("P2", 5, 9);
        verifyPlayerStats("P3", 7, 10);
        verifyPlayerStats("P4", 4, 11);
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}