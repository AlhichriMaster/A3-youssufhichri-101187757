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
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {Main.class, TestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@ActiveProfiles({"test", "two-winner-test"})
public class TwoWinnerGameTwoWinnerQuestTest {

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
//        deckService.setTestScenario("TWO_WINNER");
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
            System.out.println("We want to trim these cards: " + cardId);
            waitAndClick(By.xpath(
                    "//div[contains(@class, 'card')]//h4[text()='" + cardId + "']/parent::div"));
        }
        waitAndClick(By.xpath("//button[text()='Confirm Discard']"));
        waitForStateUpdate();
        refreshGameState();
    }

    private void sponsorQuest(String foeCard, String... weaponCards) {
        waitAndClick(By.xpath("//div[contains(@class, 'card')]//h4[text()='" + foeCard + "']/parent::div"));
        for (String weaponCard : weaponCards) {
            waitAndClick(By.xpath(
                    "//div[contains(@class, 'card')]//h4[text()='" + weaponCard + "']/parent::div"));
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
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testTwoWinnerQuest() {
        // Initial state verification
        verifyPlayerStats("P1", 0, 12);
        verifyPlayerStats("P2", 0, 12);
        verifyPlayerStats("P3", 0, 12);
        verifyPlayerStats("P4", 0, 12);

        for(Player player : game.getPlayers() ){
            System.out.println("Starting " + player.getId() + " Hand: ");
            for(Card card: player.getHand()){
                System.out.println(card.getId());
            }
        }

        // First Quest
        waitAndClick(By.id("draw-button"));
        waitForStateUpdate();
        waitAndClick(By.xpath("//button[text()='Accept']"));
        waitForStateUpdate();

        sponsorQuest("F5");  // Stage 1
        sponsorQuest("F5", "D5");  // Stage 2
        sponsorQuest("F10", "H10");  // Stage 3
        sponsorQuest("F10", "B15");  // Stage 4

        // P2's participation
        waitAndClick(By.xpath("//button[text()='Join Quest']"));
        handleHandTrim("F5");
        performAttack("H10");

        // P3 accepts/loses
        waitAndClick(By.xpath("//button[text()='Join Quest']"));
        handleHandTrim("F5");
        performAttack();
        waitForStateUpdate();

        // P4's participation
        waitAndClick(By.xpath("//button[text()='Join Quest']"));
        handleHandTrim("F10");
        performAttack("H10");

        // Stage 2
        performAttack("S10"); // P2 attack
        performAttack("S10"); // P4 attack

        // Stage 3
        performAttack("H10", "S10"); // P2 attack
        performAttack("H10", "S10"); // P4 attack

        // Stage 4
        performAttack("S10", "B15"); // P2 attack
        performAttack("S10", "B15"); // P4 attack

        // P1 hand trimming
        handleHandTrim("F5", "F10", "F15", "F15");

        refreshGameState();
        verifyPlayerStats("P2", 4, 9);
        verifyPlayerStats("P4", 4, 9);

        // Second Quest
        waitAndClick(By.id("draw-button"));
        waitForStateUpdate();
        waitAndClick(By.xpath("//button[text()='Decline']"));
        waitForStateUpdate();

        waitAndClick(By.xpath("//button[text()='Accept']"));
        waitForStateUpdate();

        sponsorQuest("F5");  // Stage 1
        sponsorQuest("F5", "D5");  // Stage 2
        sponsorQuest("F5", "H10");  // Stage 3

        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P4
        performAttack("D5");

        waitAndClick(By.xpath("//button[text()='Decline']")); // P1

        waitAndClick(By.xpath("//button[text()='Join Quest']")); // P2
        performAttack("D5");

        performAttack("B15"); // P4 stage 2
        performAttack("B15"); // P2 stage 2

        performAttack("E30"); // P4 stage 3
        performAttack("E30"); // P2 stage 3

        refreshGameState();
        waitForStateUpdate();

        System.out.println("P3 hand size: " + game.getPlayers().get(2).getHand().size());

        verifyPlayerStats("P1", 0, 12);
        verifyPlayerStats("P2", 7, 9);
        verifyPlayerStats("P3", 0, 7);
        verifyPlayerStats("P4", 7, 9);

        verifyPlayerHand("P1", "F15", "F15", "F20", "F20", "F20", "F20", "F25", "F25", "F30", "H10", "B15", "L20");
        verifyPlayerHand("P2", "F10", "F15", "F15", "F25", "F30", "F40", "F50", "L20", "L20");
        verifyPlayerHand("P3", "F40", "D5", "D5", "H10", "H10", "H10", "H10");
        verifyPlayerHand("P4", "F15", "F15", "F20", "F25", "F30", "F50", "F70", "L20", "L20");

    }


    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}