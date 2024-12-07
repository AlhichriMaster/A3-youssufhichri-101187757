package selenium;

import org.example.Main;
import org.example.model.Game;
import org.example.service.DeckService;
import org.example.service.GameService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {Main.class, TestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@ActiveProfiles({"test", "zero-winner-test"})
public class zeroWinnerQuest {
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



}
