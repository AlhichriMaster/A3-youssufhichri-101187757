import org.example.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.util.Scanner;



public class MainTest {

    private Game newGame;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    void setUp() {
        newGame = new Game();
        newGame.setupGame();
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    @DisplayName("Main function initializes 4 players")
    void RESP_1_TEST_01(){
        //test 1: Assert that the players have been initialized and are not null
        List<Player> players = newGame.getPlayers();
        assertNotNull(players);
    };


    @Test
    @DisplayName("Adventure deck is initialized and has 100 cards")
    void RESP_2_TEST_01(){
        //test 2: Adventure deck is initialized
        Deck adventureDeck = newGame.getAdventureDeck();
        assertNotNull(adventureDeck);
        //has to change to 52 instead of 100, because now player hand is initialized everytime in setupGame();
        assertEquals(52, adventureDeck.getCards().size());
    };


    @Test
    @DisplayName("Event deck are and has 17 cards")
    void RESP_3_TEST_01(){
        //test 2: check if the event deck were initialized
        Deck eventDeck = newGame.getEventDeck();
        assertNotNull(eventDeck);
        assertEquals(17, eventDeck.getCards().size());

    };


    @Test
    @DisplayName("Each player has a 12 card hand")
    void RESP_4_TEST_01(){
        List<Player> players = newGame.getPlayers();

        //check if each player has 12 cards in his hand
        for (Player curr : players) {
            assertEquals(12, curr.getHand().size());
        }

        assertEquals(52, newGame.getAdventureDeck().getCards().size());
    };


    @Test
    @DisplayName("Game displays whose turn it is")
    void RESP_5_TEST_01() {

        String input = "\n\n\n\n\n\n\n\n\n\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        for (int i = 3; i <= 6; i++) {
            newGame.moveToNextPlayer();
            String expectedOutput = "It's P" + ((i + 2) % 4 + 1) + "'s turn.";
//            System.out.println("It's " + currentPlayer.getId() + "'s turn.");
            assertTrue(outputStreamCaptor.toString().contains(expectedOutput));

        }

        // Reset System.in
        System.setIn(System.in);
    }


    @Test
    @DisplayName("Game displays current players hand")
    void RESP_6_TEST_01() {
        Player currentPlayer = newGame.getCurrentPlayer();

        // Clear the player's hand and add known cards
        currentPlayer.getHand().clear();
        currentPlayer.getHand().add(new AdventureCard("S10", CardType.WEAPON, 10)); // Sword
        currentPlayer.getHand().add(new AdventureCard("F15", CardType.FOE, 15));    // Foe
        currentPlayer.getHand().add(new AdventureCard("H10", CardType.WEAPON, 10)); // Horse

        currentPlayer.sortHand();

        // Capture the output
        System.setOut(new PrintStream(outputStreamCaptor));

        // Action
        newGame.displayPlayerHand(currentPlayer);


        // Assert
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains("Current player's hand:"));
        assertTrue(output.contains("S10")); // Sword
        assertTrue(output.contains("F15")); // Foe
        assertTrue(output.contains("H10")); // Horse


        // Verify the order: Foes first, then weapons
        int foeIndex = output.indexOf("F15");
        int swordIndex = output.indexOf("S10");
        int horseIndex = output.indexOf("H10");
        assertTrue(foeIndex < swordIndex);
        assertTrue(foeIndex < horseIndex);

        // Reset System.out
        System.setOut(System.out);
    };

    @Test
    @DisplayName("Game draws event card")
    void RESP_7_TEST_01() {
        EventCard drawnCard = newGame.drawEventCard();
        assertNotNull(drawnCard);
        assertEquals(16, newGame.getEventDeck().getCards().size());
    }

    @Test
    @DisplayName("Game handles Plague event card")
    void RESP_8_TEST_01() {
        Player currentPlayer = newGame.getCurrentPlayer();
        currentPlayer.addShields(5);

        String input = "\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(new EventCard("Plague", EventType.PLAGUE));
        assertEquals(3, currentPlayer.getShields());
    }

    @Test
    @DisplayName("Game handles Queen's favor event card")
    void RESP_9_TEST_01() {
        Player currentPlayer = newGame.getCurrentPlayer();
        int initialHandSize = currentPlayer.getHand().size();

        String input = "0\n0\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(new EventCard("Queen's Favor", EventType.QUEENS_FAVOR));
        if(currentPlayer.getHand().size() > 10){
            //make sure we never go past 12 cards in a hand
            assertEquals(12, currentPlayer.getHand().size());
        }else{
            assertEquals(initialHandSize + 2, currentPlayer.getHand().size());
        }

    }


    @Test
    @DisplayName("Game handles Prosperity event card")
    void RESP_10_TEST_01() {
        Player currentPlayer = newGame.getCurrentPlayer();
        int initialHandSize = currentPlayer.getHand().size();


        String input = "0\n0\n\n0\n0\n\n0\n0\n\n0\n0\n\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(new EventCard("Prosperity", EventType.PROSPERITY));
        if(currentPlayer.getHand().size() > 10){
            //make sure we never go past 12 cards in a hand
            assertEquals(12, currentPlayer.getHand().size());
        }else{
            assertEquals(initialHandSize + 2, currentPlayer.getHand().size());
        }
    }

    @Test
    @DisplayName("Game prompts players for quest sponsorship (Quest Card)")
    void RESP_11_TEST_01() {
        newGame.initializePlayersHands();
        String input = "n\nn\nn\nn\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(new EventCard("Q1", EventType.QUEST));
        assertTrue(outputStreamCaptor.toString().trim().contains("do you want to sponsor this quest? (y/n)"));
    }


    @Test
    @DisplayName("Sponsor sets up a quest with the correct number of stages")
    void RESP_12_TEST_01() {

        EventCard curr = new EventCard("Q2", EventType.QUEST);


        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F20", CardType.FOE, 20));
        hand.add(new AdventureCard("F25", CardType.FOE, 25));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));

        // Set up player hands to ensure they cant sponsor
        for (Player player : newGame.getPlayers()) {
            player.getHand().clear();
            player.getHand().addAll(hand);
        }

        String input = "y\n0\ndone\n1\ndone\n0\ndone\n" + //sponsoring and setting up quest
                        "y\nn\nn\n" + //do you want to participate
                        "0\nc\n2\ndone\n" + //setting up the attack for stage 1
                        "0\nc\n2\ndone\n" + //setting up attack for stage 2
                        "\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(curr);

        assertEquals(2, newGame.getCurrentQuest().getStages());
    }

    @Test
    @DisplayName("Sponsor sets up a quest with 1 foe in each stage")
    void RESP_12_TEST_02() {
        EventCard curr = new EventCard("Q2", EventType.QUEST);


        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F20", CardType.FOE, 20));
        hand.add(new AdventureCard("F25", CardType.FOE, 25));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));

        // Set up player hands to ensure they cant sponsor
        for (Player player : newGame.getPlayers()) {
            player.getHand().clear();
            player.getHand().addAll(hand);
        }

        String input = "y\n0\ndone\n1\ndone\n0\ndone\ny\nn\nn\n0\ndone\n0\ndone\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(curr);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Invalid stage. It must have a foe and be stronger than the previous stage."));
    }


    @Test
    @DisplayName("Cards are removed from the sponsors hands when used")
    void RESP_12_TEST_03() {
        EventCard curr = new EventCard("Q2", EventType.QUEST);


        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F20", CardType.FOE, 20));
        hand.add(new AdventureCard("F25", CardType.FOE, 25));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));

        // Set up player hands to ensure they cant sponsor
        for (Player player : newGame.getPlayers()) {
            player.getHand().clear();
            player.getHand().addAll(hand);
        }

        String input = "y\n0\n1\ndone\n1\ndone\ny\nn\nn\nc\n0\ndone\nc\n0\ndone\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(curr);

        //used to check if its 0, but now, sponsor has to draw at the end of a quest. So making sure its not 12, but 8 instead
        assertEquals(8, newGame.getCurrentPlayer().getHand().size());
    }



    @Test
    @DisplayName("Game prompts players (excluding sponsor) for quest participation")
    void RESP_13_TEST_01() {

        EventCard curr = new EventCard("Q2", EventType.QUEST);


        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F20", CardType.FOE, 20));
        hand.add(new AdventureCard("F25", CardType.FOE, 25));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));

        // Set up player hands to ensure they cant sponsor
        for (Player player : newGame.getPlayers()) {
            player.getHand().clear();
            player.getHand().addAll(hand);
        }

        String input = "y\n0\ndone\n0\ndone\ny\nn\nn\n0\ndone\n0\ndone\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(curr);

        assertTrue(outputStreamCaptor.toString().trim().contains("do you want to participate in this quest? (y/n)"));
    }


    @Test
    @DisplayName("Game allows participants to set up attacks")
    void RESP_14_TEST_01() {
        Player participant = newGame.getPlayers().get(1);
        Stage stage = new Stage();
        stage.addCard(new AdventureCard("F10", CardType.FOE, 10));

        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F10", CardType.FOE, 10));
        hand.add(new AdventureCard("F20", CardType.FOE, 20));
        hand.add(new AdventureCard("W15", CardType.WEAPON, 15));
        participant.setHand(hand);

        String input = "2\ndone\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        List<Card> attack = newGame.setupAttack(participant);
        assertFalse(attack.isEmpty());
        assertTrue(attack.stream().allMatch(card -> card instanceof AdventureCard && ((AdventureCard) card).getType() == CardType.WEAPON));
    }


    @Test
    @DisplayName("Game properly calculates the value of an attack")
    void RESP_15_TEST_01() {
        Player participant = newGame.getPlayers().get(1);
        Stage stage = new Stage();
        stage.addCard(new AdventureCard("F10", CardType.FOE, 10));

        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F10", CardType.FOE, 10));
        hand.add(new AdventureCard("F20", CardType.FOE, 20));
        hand.add(new AdventureCard("W15", CardType.WEAPON, 15));
        participant.setHand(hand);

        String input = "2\ndone\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        List<Card> attack = newGame.setupAttack(participant);
        assertEquals(15, newGame.calculateAttackValue(attack));
    }


    @Test
    @DisplayName("Game resolves quest, and gives shields to winners")
    void RESP_16_TEST_01() {
        EventCard curr = new EventCard("Q2", EventType.QUEST);


        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F20", CardType.FOE, 20));
        hand.add(new AdventureCard("F25", CardType.FOE, 25));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));

        // Set up player hands to ensure they cant sponsor
        for (Player player : newGame.getPlayers()) {
            player.getHand().clear();
            player.getHand().addAll(hand);
        }


        String input = "y\n0\ndone\n0\ndone\n" + //sponsoring
                        "y\nn\nn\n" + //deciding to participate
                        "c\n5\n4\ndone\nc\n4\n3\ndone\n\n\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(curr);

        assertEquals(2, newGame.getPlayers().get(1).getShields());

    }




    @Test
    @DisplayName("Game makes sure that the player has the cards to sponsor a quest")
    void RESP_17_TEST_01() {

        EventCard curr = new EventCard("Q2", EventType.QUEST);


        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F20", CardType.FOE, 20));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));

        // Set up player hands to ensure they cant sponsor
        for (Player player : newGame.getPlayers()) {
            player.getHand().clear();
            player.getHand().addAll(hand);
        }

        String input = "\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(curr);

        String output = outputStreamCaptor.toString();
        //assert that the notification is in the output
        assertTrue(output.contains("Quest card drawn, but no one can sponsor. Ignoring."));
        //assert that we dont ask for a sponsor
        assertFalse(output.contains("do you want to sponsor this quest? (y/n)"));
    }


    @Test
    @DisplayName("Game allows a player to choose the cards they want to trim")
    void RESP_18_TEST_01() {
        String input = "2\n0\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        Player player = newGame.getPlayers().getFirst();

        player = new Player("TestPlayer");
        for (int i = 0; i < 14; i++) {
            player.getHand().add(new AdventureCard("Card" + i, CardType.FOE, i));
        }

        player.trimHand(scanner);

        assertEquals(12, player.getHand().size());
        assertFalse(player.getHand().stream().anyMatch(card -> card.getId().equals("Card2")));
        assertFalse(player.getHand().stream().anyMatch(card -> card.getId().equals("Card0")));
    }


    @Test
    @DisplayName("Game clears the 'hotseat' and flushes the screen")
    void RESP_19_TEST_01() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("\n\n\n\n".getBytes());
        System.setIn(inputStream);
        newGame.setScanner(new Scanner(System.in));

        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        newGame.moveToNextPlayer();

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("your turn is over. Press Enter to leave the hot seat."));
        assertTrue(output.contains("Screen flushed. Press Enter to continue..."));
        assertTrue(output.contains("It's P2's turn."));
        assertTrue(output.contains("Press Enter to view your hand."));

        // Reset System.in and System.out
        System.setIn(System.in);
        System.setOut(System.out);
    }


    @Test
    @DisplayName("Game showcases the cards used for the current stage")
    void RESP_20_TEST_01() {

        EventCard curr = new EventCard("Q2", EventType.QUEST);


        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F20", CardType.FOE, 20));
        hand.add(new AdventureCard("F25", CardType.FOE, 25));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));


        for (Player player : newGame.getPlayers()) {
            player.getHand().clear();
            player.getHand().addAll(hand);
        }

        String input = "y\n0\n1\ndone\n0\n0\ndone\nn\nn\nn\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(curr);

        String output = outputStreamCaptor.toString();
        //Stage 1
        assertTrue(output.contains("Successfully built stage 1"));
        assertTrue(output.contains("Cards used for this stage: "));
        assertTrue(output.contains("Selected Foe: F20"));
        assertTrue(output.contains("Selected Weapon Cards: W30"));

        //Stage 2
        assertTrue(output.contains("Successfully built stage 2"));
        assertTrue(output.contains("Cards used for this stage: "));
        assertTrue(output.contains("Selected Foe: F25"));
        assertTrue(output.contains("Selected Weapon Cards: W45 "));
    }


    @Test
    @DisplayName("Game showcases the attack cards picked for this stage")
    void RESP_21_TEST_01() {

//        newGame.initializePlayersHands();

        EventCard curr = new EventCard("Q2", EventType.QUEST);


        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F20", CardType.FOE, 20));
        hand.add(new AdventureCard("F25", CardType.FOE, 25));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));

        // Set up player hands to ensure they can participate
        for (Player player : newGame.getPlayers()) {
            player.getHand().clear();
            player.getHand().addAll(hand);
        }

        String input = "y\n0\ndone\n0\ndone\ny\nn\nn\n0\n4\ndone\n0\n4\ndone\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(curr);

        String output = outputStreamCaptor.toString();
        //Stage 1 attack
        assertTrue(output.contains("Successfully built attack for this stage"));
        assertTrue(output.contains("Weapon Cards used in this attack: W45 "));

        //Stage 2 attack
        assertTrue(output.contains("Weapon Cards used in this attack: W45 "));
    }



    @Test
    @DisplayName("Game makes sure that the player has the cards to sponsor a quest")
    void RESP_22_TEST_01() {
        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F10", CardType.FOE, 10));
        hand.add(new AdventureCard("F20", CardType.FOE, 20));

        //Set each players hand to the custom hand above.
        //Now that we have every player with 2 foe cards, no one should get prompted to 'sponsor' because no one can sponsor
        newGame.getPlayers().getFirst().setHand(hand);


        List<Card> participantHand = new ArrayList<>();
        participantHand.add(new AdventureCard("W15", CardType.WEAPON, 15));

        //Set each players hand to the custom hand above.
        //Now that we have every player with 2 foe cards, no one should get prompted to 'sponsor' because no one can sponsor
        for(Player player : newGame.getPlayers()){
            if(!player.getId().equals("P1") && !player.getId().equals("P2")){
                player.setHand(participantHand);
            }
        }


        EventCard curr = new EventCard("Q2", EventType.QUEST);

        String input = "y\n0\ndone\n0\ndone\ny\nn\nn\n0\ndone\n0\ndone\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(curr);

        String output = outputStreamCaptor.toString();
        //assert that the notification is in the output
        assertTrue(output.contains("P2, do you want to participate in this quest? (y/n)"));
        //assert that we dont ask for a participant
        assertFalse(output.contains("P3, do you want to participate in this quest? (y/n)"));
        assertFalse(output.contains("P4, do you want to participate in this quest? (y/n)"));
    }



    @Test
    @DisplayName("Game allows participants to withdraw from a quest and become ineligible")
    void RESP_23_TEST_01() {
//        newGame.initializePlayersHands();

        // Set up a quest card
        EventCard questCard = new EventCard("Q2", EventType.QUEST);


        List<Card> hand = new ArrayList<>();
        hand.add(new AdventureCard("F20", CardType.FOE, 20));
        hand.add(new AdventureCard("F25", CardType.FOE, 25));
        hand.add(new AdventureCard("W30", CardType.WEAPON, 30));
        hand.add(new AdventureCard("W45", CardType.WEAPON, 45));

        // Set up player hands to ensure they can participate
        for (Player player : newGame.getPlayers()) {
            player.getHand().clear();
            player.getHand().addAll(hand);
        }

        // Simulate user input:
        // P1 sponsors, P2 participates and withdraws, P3 participates and continues, P4 doesn't participate
        String input = "y\n0\ndone\n0\ndone\n" +
                        "y\ny\nn\n" +
                        "w\nc\n4\ndone\nc\n4\ndone\nc\n\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(questCard);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("P2 has withdrawn from the quest."));
        assertTrue(output.contains("P3 passed stage 1"));
        assertFalse(output.contains("P2 passed stage 2"));
        assertTrue(output.contains("P3 passed stage 2"));

        // Reset System.in
        System.setIn(System.in);
    }


    @Test
    @DisplayName("Eligibile participants draw 1 card, and trims their hand if necessary")
    void RESP_24_TEST_01() {
//        newGame.initializePlayersHands();

        // Set up a quest card
        EventCard questCard = new EventCard("Q2", EventType.QUEST);

        // Set up player hands
        Player sponsor = newGame.getPlayers().get(0);
        Player participant1 = newGame.getPlayers().get(1);

        sponsor.getHand().clear();
        sponsor.getHand().add(new AdventureCard("F10", CardType.FOE, 10));
        sponsor.getHand().add(new AdventureCard("F20", CardType.FOE, 20));

        participant1.getHand().clear();
        participant1.getHand().add(new AdventureCard("W15", CardType.WEAPON, 15));
        participant1.getHand().add(new AdventureCard("W20", CardType.WEAPON, 20));

        // Simulate user input
        String input = "y\n0\ndone\n0\ndone\n" + // Sponsor sets up quest
                "y\nn\nn\nc\n2\ndone\nc\n2\ndone\n\n\n"; // Participant 1 plays
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(questCard);

        String output = outputStreamCaptor.toString();

        // Check if participants drew a card
        assertTrue(output.contains("drew a card"));
        //the participant drew a card. He had 2 cards, used 2 and drew once for each stage so should have size 2 hand
        assertEquals(2, participant1.getHand().size());
    }



    @Test
    @DisplayName("Quest sponsor draws cards after quest ends. (# of stages + # of cards used in quest)")
    void RESP_25_TEST_01() {
//        newGame.initializePlayersHands();

        // Set up a quest card
        EventCard questCard = new EventCard("Q2", EventType.QUEST);

        // Set up player hands
        Player sponsor = newGame.getPlayers().get(0);
        Player participant1 = newGame.getPlayers().get(1);

        sponsor.getHand().clear();
        sponsor.getHand().add(new AdventureCard("F10", CardType.FOE, 10));
        sponsor.getHand().add(new AdventureCard("F20", CardType.FOE, 20));

        participant1.getHand().clear();
        participant1.getHand().add(new AdventureCard("W15", CardType.WEAPON, 15));
        participant1.getHand().add(new AdventureCard("W20", CardType.WEAPON, 20));

        // Simulate user input
        String input = "y\n0\ndone\n0\ndone\n" + // Sponsor sets up quest
                "y\nn\nn\nc\n0\ndone\nc\n0\ndone\n\n\n"; // Participant 1 plays
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        newGame.setScanner(new Scanner(System.in));

        newGame.handleEventCard(questCard);

        String output = outputStreamCaptor.toString();

        //Sponsor starts with 2 cards, uses both for quest. Now needs to draw cards = number of cards used + number of stages in quest => 4 (in this case)
        assertEquals(4, sponsor.getHand().size());
    }

    @Test
    @DisplayName("Game checks if there is a winner and terminates if a winner is found")
    void RESP_26_TEST_01() {

        //rig the player to have enough shields to win
        newGame.getPlayers().getFirst().addShields(7);

        //play the game, should never actually start and immediately terminate with the winner announcement
        newGame.playGame();

        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("Game Over!"));
        assertTrue(output.contains("P1 wins with 7 shields!"));
    }
}