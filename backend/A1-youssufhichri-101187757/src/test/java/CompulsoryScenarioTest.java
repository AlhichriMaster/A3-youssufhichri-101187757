//import org.example.dto.enums.CardType;
//import org.example.dto.enums.EventType;
//import org.example.model.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.DisplayName;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.util.*;
//
//public class CompulsoryScenarioTest {
//
//    @Mock
//    private Deck mockDeck;
//
//    private Game game;
//    private ByteArrayOutputStream outputStreamCaptor;
//    private Queue<Card> riggedDraws;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.initMocks(this);  // or MockitoAnnotations.openMocks(this) for newer Mockito versions
//
//        // Manually create the Game instance
//        game = new Game();
//        game.setupGame();
//
//        outputStreamCaptor = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(outputStreamCaptor));
//
//        riggedDraws = new LinkedList<>();
//
//        // Stub the drawCard() method to return cards from the riggedDraws queue
//        when(mockDeck.drawCard()).thenAnswer(invocation -> {
//            if (riggedDraws.isEmpty()) {
//                System.out.println("Drawing default card: DefaultCard");
//                return new AdventureCard("DefaultCard", CardType.FOE, 1);
//            }
//            Card drawnCard = riggedDraws.poll();
//            System.out.println("Drawing rigged card: " + drawnCard.getId());
//            return drawnCard;
//        });
//        doNothing().when(mockDeck).shuffle();
//
//        // Set the mocked deck in the game
//        game.setAdventureDeck(mockDeck);
//    }
//
//
//    @Test
//    @DisplayName("A-TEST JP-Scenario")
//    void jpScenario() {
//        rigInitialHands();
//        rigCardDraws();  // Prepare rigged draws
//
//        EventCard questCard = new EventCard("Q4", EventType.QUEST);
//
//        String input = constructUserInput();
//        System.setIn(new ByteArrayInputStream(input.getBytes()));
//        game.setScanner(new Scanner(System.in));
//
//        game.handleEventCard(questCard);  // This will now use the mocked drawCard method
//
////        verify(mockDeck, times(10)).drawCard();
//
//        Player p1 = game.getPlayers().get(0);
//        Player p2 = game.getPlayers().get(1);
//        Player p3 = game.getPlayers().get(2);
//        Player p4 = game.getPlayers().get(3);
//
//        // Assert final states (these checks are done as per your game logic)
//        assertEquals(0, p1.getShields());
//        assertHandContains(p1, "F5", "F10", "F15", "F15", "F30", "H10", "B15", "B15", "L20");
//
//        assertEquals(0, p2.getShields());
//        assertEquals(12, p2.getHand().size());
//
//        assertEquals(0, p3.getShields());
//        assertHandContains(p3, "F5", "F5", "F15", "F30", "S10");
//
//        assertEquals(4, p4.getShields());
//        assertHandContains(p4, "F15", "F15", "F40", "L20");
//
//        // Reset System.in
//        System.setIn(System.in);
//    }
//
//    // The rigging methods for initial hands and card draws stay the same
//    private void rigInitialHands() {
//        List<List<String>> initialHands = Arrays.asList(
//                Arrays.asList("F5", "F5", "F15", "F15", "D5", "S10", "S10", "H10", "H10", "B15", "B15", "L20"),
//                Arrays.asList("F5", "F5", "F15", "F15", "F40", "D5", "S10", "H10", "H10", "B15", "B15", "E30"),
//                Arrays.asList("F5", "F5", "F5", "F15", "D5", "S10", "S10", "S10", "H10", "H10", "B15", "L20"),
//                Arrays.asList("F5", "F15", "F15", "F40", "D5", "D5", "S10", "H10", "H10", "B15", "L20", "E30")
//        );
//
//        for (int i = 0; i < 4; i++) {
//            Player player = game.getPlayers().get(i);
//            player.getHand().clear();
//            for (String cardId : initialHands.get(i)) {
//                player.getHand().add(createCardFromId(cardId));
//            }
//        }
//    }
//
//    private void rigCardDraws() {
//        riggedDraws.clear(); // Clear any existing cards
//        String[] cardsToDraw = {"F30", "S10", "B15", "F10", "L20", "L20", "B15", "S10", "F30", "L20"};
//        for (String cardId : cardsToDraw) {
//            Card card = createCardFromId(cardId);
//            riggedDraws.add(card);
//            System.out.println("Added to rigged draws: " + card.getId());
//        }
//    }
//
//    private AdventureCard createCardFromId(String id) {
//        char type = id.charAt(0);
//        int value = Integer.parseInt(id.substring(1));
//        return new AdventureCard(id, type == 'F' ? CardType.FOE : CardType.WEAPON, value);
//    }
//
//    private String constructUserInput() {
//        return String.join("\n",
//                "n", // P1 declines to sponsor
//                "y", // P2 sponsors
//                "0", "6", "done", // Stage 1 - F5 and H10
//                "1", "4", "done", // Stage 2 - F15 and S10
//                "1", "2", "3", "done", // Stage 3 - F15 and D5 and B15
//                "1", "2", "done", // Stage 4
//                "y", "y", "y", // P1 P3 and P4 participate
//
//                //stage 1
//                "0", "0", "0", //loop through the participants where they all draw the cards
//                "c", "4", "4", "done", // P1 decides to continue, and preps their attack
//                "c", "4", "3", "done", // P3 decides to continue, and preps their attack
//                "c", "3", "5", "done", // P4 decides to continue, and preps their attack
//
//                //stage 2
//                "c", "6", "5", "done", // P1 pulls a card and is asked if they want to withdraw
//                "c", "8", "3", "done", // P3 participates
//                "c", "5", "6", "done", // P4 participates
//
//                //stage 3
//                "c", "9", "5", "3", "done", // P3 attack
//                "c", "6", "4", "5", "done", // P4 attack
//
//                //stage 4
//                "c", "6", "5", "5", "done", // P3 attack
//                "c", "3", "3", "4", "4", "done", // P4 attack
//
//                "0", "0", "0", "0",  //P2 trims down their hand from 16 -> 12
//                "\n" //end the game
//        );
//    }
//
//    private void assertHandContains(Player player, String... cardIds) {
//        List<String> hand = player.getHand().stream()
//                .map(Card::getId)
//                .toList();
//        assertTrue(hand.containsAll(Arrays.asList(cardIds)),
//                "Player " + player.getId() + "'s hand should contain " + Arrays.toString(cardIds) +
//                        " but actually contains " + hand);
//    }
//}
