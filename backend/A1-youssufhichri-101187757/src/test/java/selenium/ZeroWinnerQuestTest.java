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
@ActiveProfiles({"test", "zero-winner-test"})
public class ZeroWinnerQuestTest {
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
        if (weaponCards.length == 0) {
            try {
                waitAndClick(By.xpath("//button[text()='Withdraw']"));
            } catch (Exception e) {
                try {
                    waitAndClick(By.xpath("//button[text()='Continue']"));
                } catch (Exception ex) {
                    System.out.println("Could not find withdrawal or continue button");
                    throw ex;
                }
            }
        } else {
            for (String weaponCard : weaponCards) {
                waitAndClick(By.xpath(
                        "//div[contains(@class, 'card')]//h4[text()='" + weaponCard + "']/parent::div"));
            }
            waitAndClick(By.xpath("//button[text()='Confirm Attack']"));
        }
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
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testZeroWinnerQuest() {
        // Initial state verification
        verifyPlayerStats("P1", 0, 12);
        verifyPlayerStats("P2", 0, 12);
        verifyPlayerStats("P3", 0, 12);
        verifyPlayerStats("P4", 0, 12);

        // Draw and sponsor quest
        waitAndClick(By.id("draw-button"));
        waitForStateUpdate();
        waitAndClick(By.xpath("//button[text()='Accept']"));
        waitForStateUpdate();

        // P1 sponsors quest stages
        sponsorQuest("F50", "D5", "S10", "H10", "B15", "L20");  // Stage 1
        sponsorQuest("F70", "D5", "S10", "H10", "B15", "L20");  // Stage 2

        // Stage 1 participation
        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P2
        handleHandTrim("F5");
        performAttack("E30");

        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P3
        handleHandTrim("F15");
        performAttack();

        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P4
        handleHandTrim("F10");
        performAttack();

        // P1 draws and discards after quest
        handleHandTrim("F5", "F10");

        // Verify final state
        refreshGameState();
        verifyPlayerStats("P1", 0, 12);
        verifyPlayerStats("P2", 0, 12);
        verifyPlayerStats("P3", 0, 12);
        verifyPlayerStats("P4", 0, 12);

        verifyPlayerHand("P1", "F15", "D5", "D5", "D5", "D5", "S10", "S10", "S10", "H10", "H10", "H10", "H10");
        verifyPlayerHand("P2", "F5", "F5", "F10", "F15", "F15", "F20", "F20", "F25", "F30", "F30", "F40", "E30");
        verifyPlayerHand("P3", "F5", "F5", "F10", "F15", "F15", "F20", "F20", "F25", "F25", "F30", "F40", "L20");
        verifyPlayerHand("P4", "F5", "F5", "F10", "F15", "F15", "F20", "F20", "F25", "F25", "F30", "F50", "E30");

    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}