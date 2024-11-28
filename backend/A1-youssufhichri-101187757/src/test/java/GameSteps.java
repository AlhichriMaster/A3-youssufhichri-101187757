//import io.cucumber.java.en.*;
//import io.cucumber.datatable.DataTable;
//import org.example.dto.enums.CardType;
//import org.example.dto.enums.EventType;
//import org.example.model.*;
//
//import java.io.*;
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//
//// A1ScenarioSteps.java
//public class GameSteps {
//    private Game game;
//    private Queue<Card> riggedDraws = new LinkedList<>();
//    private List<Card> riggedDrawsForP2 = new LinkedList<>();
//    private ByteArrayInputStream inputStream;
//    private String simulatedInput;
//    private EventCard questCard;
//    private Map<Integer, List<Integer>> usedCardIndices = new HashMap<>();
//    private Map<Integer, List<Card>> discardedCardsByPlayer = new HashMap<>();
//    private Map<Integer, Map<String, Integer>> remainingCards = new HashMap<>();
//
//
//    @Given("a new game is initialized with {int} players")
//    public void setupGame(int numPlayers) {
//        game = new Game();
//        game.setupGame();
//        simulatedInput = "";
//    }
//
//    @Given("player {string}'s hand is initialized {int}")
//    public void setupPlayerHand(String playerId, int scenario) {
//        resetPlayerHands(scenario);
//    }
//
//    private Player findPlayerById(String id) {
//        return game.getPlayers().stream()
//                .filter(p -> p.getId().equals(id))
//                .findFirst()
//                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + id));
//    }
//
//    @When("P{int} draws a quest card {string}")
//    public void drawQuestCard(int playerNum, String questId) {
//        game.setCurrentPlayer(game.getPlayers().get(playerNum - 1));
//        questCard = new EventCard(questId, EventType.QUEST);
//    }
//
//    @When("P{int} draws an event card {string}")
//    public void drawEventCard(int playerNum, String eventType) {
//        game.setCurrentPlayer(game.getPlayers().get(playerNum - 1));
//
//        switch (eventType){
//            case "plague":
//                questCard = new EventCard(eventType, EventType.PLAGUE);
//                break;
//
//            case "prosperity":
//                questCard = new EventCard(eventType, EventType.PROSPERITY);
//                for(Player player : game.getPlayers()) {
//                    if (player.getHand().size() > 10) {
//                        simulatedInput += player.getHand().size()-1 + "\n";
//                    }
//                }
//                break;
//
//            case "Qfavor":
//                questCard = new EventCard(eventType, EventType.QUEENS_FAVOR);
//                break;
//        }
//    }
//
//
//    private void simulateEnterPress() {
//        simulatedInput += "\n";
//        provideInput(simulatedInput);
//    }
//
//    @When("P{int} presses enter")
//    public void playerPressesEnter(int playerNum) {
//        simulateEnterPress();
//    }
//
//
//
//    @When("the following cards are rigged to be drawn in order:")
//    public void rigCardDraws(DataTable dataTable) {
//        List<Map<String, String>> cards = dataTable.asMaps(String.class, String.class);
//        for (Map<String, String> card : cards) {
//            riggedDraws.add(createCardFromId(card.get("cardId")));
//        }
//
//        Deck adventureDeck = new Deck();
//        for(Card card : riggedDraws){
//            adventureDeck.addFirst(card);
//        }
//
//
//        for(Card card : adventureDeck.getCards()){
//            game.getAdventureDeck().addLast(card);
//        }
//
//        riggedDrawsForP2 = new LinkedList<>(game.getAdventureDeck().getCards());
//    }
//
//
//
//
//    @When("P{int} declines to sponsor the quest")
//    public void declineQuestSponsorship(int playerNum) {
//        simulatedInput += "n\n";
//        provideInput(simulatedInput);
//    }
//
//    @When("P{int} accepts to sponsor the quest")
//    public void acceptQuestSponsorship(int playerNum) {
//        simulatedInput += "y\n";
//        provideInput(simulatedInput);
//    }
//
//    @When("P{int} agrees to participate in the quest")
//    public void acceptQuestParticipation(int playerNum) {
//        simulatedInput += "y\n";
//        provideInput(simulatedInput);
//    }
//
//    @When("P{int} declines to participate in the quest")
//    public void declineQuestParticipation(int playerNum) {
//        simulatedInput += "n\n";
//        provideInput(simulatedInput);
//    }
//
//    @When("P{int} continues and attacks stage {int} with {string}")
//    public void playerAttacksStage(int playerNum, int stageNumber, String attackCards) {
//        simulatedInput += "c\n"; // Continue response
//
//        // Add each attack card
//        String[] cards = attackCards.split(",");
//
//        for (String card : cards) {
//            String trimmedCard = card.trim();
//            int index = findCardIndex(game.getPlayers().get(playerNum - 1), trimmedCard);
//            simulatedInput += index + "\n";
//        }
//
//        simulatedInput += "done\n";  // Finish attack
//
//        provideInput(simulatedInput);
//
//    }
//
//    public int findCardIndex(Player player, String cardId){
//
//        List<Card> Hand = player.getHand();
//
//        for(int i = 0; i < Hand.size(); i++){
//            if (Objects.equals(Hand.get(i).getId(), cardId) ){
//                player.getHand().remove(i);
//                return i;
//            }
//        }
//
//        throw new IllegalStateException("No available card");
//    }
//
//    @When("P{int} draws a card for stage {int}")
//    public void playerDrawsCardForStage(int playerNum, int stageNumber) {
//
//        if (!riggedDraws.isEmpty()) {
//
//            Card drawnCard = riggedDraws.poll();
//
//            // Add the drawn card to the player's hand
//            Player player = game.getPlayers().get(playerNum - 1);
//
//            player.getHand().add(drawnCard);
//
//            if(player.getHand().size() > 12){
//                player.getHand().removeFirst();
//                simulatedInput += "0\n";  // Simulate drawing a card
//                provideInput(simulatedInput);
//            }
//
//            player.sortHand();
//
//        }
//    }
//
//
//
//    @When("P{int} draws {int} cards")
//    public void playerDrawsCards(int playerNum, int numberOfCardsToDraw) {
//        Player player = game.getPlayers().get(playerNum - 1);
////        List<Card> hand = player.getHand();
//
//        for(int i = 0; i < numberOfCardsToDraw; i++){
//            player.getHand().add(riggedDrawsForP2.get(i));
//        }
//    }
//
//    @When("P{int} discards all extra cards")
//    public void playerDiscardsCards(int playerNum) {
//        Player player = game.getPlayers().get(playerNum - 1);
//
//        while(player.getHand().size() > 12){
//            player.getHand().removeFirst();
//            simulatedInput += "0\n";  // Simulate drawing a card
//            provideInput(simulatedInput);
//        }
//    }
//
//    @When("Event card is handled")
//    public void processEventCard(){
//        game.handleEventCard(questCard);
//        simulatedInput = "";
//        resetPlayerHands(3);
//    }
//
//
//    @When("all quest responses are processed {int}")
//    public void processQuestResponses(int scenario) {
//        // Reset player hands
//
//        resetPlayerHands(scenario);
//        game.handleEventCard(questCard);
//
//        if(scenario == 2){
//            resetPlayerHands(scenario);
//        }
//
//        simulatedInput = "";
//    }
//
//    private void resetPlayerHands(int scenario) {
//
//        switch(scenario) {
//            case 1:
//                // code block
//                List<List<String>> initialHands1 = Arrays.asList(
//                        Arrays.asList("F5", "F5", "F15", "F15", "D5", "S10", "S10", "H10", "H10", "B15", "B15", "L20"),
//                        Arrays.asList("F5", "F5", "F15", "F15", "F40", "D5", "S10", "H10", "H10", "B15", "B15", "E30"),
//                        Arrays.asList("F5", "F5", "F5", "F15", "D5", "S10", "S10", "S10", "H10", "H10", "B15", "L20"),
//                        Arrays.asList("F5", "F15", "F15", "F40", "D5", "D5", "S10", "H10", "H10", "B15", "L20", "E30")
//                );
//
//                for (int i = 0; i < 4; i++) {
//                    Player player = game.getPlayers().get(i);
//                    player.getHand().clear();
//                    for (String cardId : initialHands1.get(i)) {
//                        player.getHand().add(createCardFromId(cardId));
//                    }
//                    player.sortHand();
//                }
//                break;
//            case 2:
//                List<List<String>> initialHands2 = Arrays.asList(
//                        Arrays.asList("F5", "F10", "F15", "F25", "D5", "D5", "H10", "B15"),
//                        Arrays.asList("D5", "D5", "D5", "H10", "B15", "B15", "B15", "L20", "E30"),
//                        Arrays.asList("F5", "F10", "F15", "D5", "D5", "D5", "D5"),
//                        Arrays.asList("D5", "D5", "D5", "H10", "B15", "B15", "B15", "L20", "E30")
//                );
//                for (int i = 0; i < 4; i++) {
//                    Player player = game.getPlayers().get(i);
//                    player.getHand().clear();
//                    for (String cardId : initialHands2.get(i)) {
//                        player.getHand().add(createCardFromId(cardId));
//                    }
//                    player.sortHand();
//                }
//                break;
//
//            case 3:
//                List<List<String>> initialHands3 = Arrays.asList(
//                        Arrays.asList("F5", "F10", "F10", "F15", "F15", "F20", "F20"),
//                        Arrays.asList("D5", "S10", "S10", "B15", "B15", "L20", "L20"),
//                        Arrays.asList("D5", "S10", "S10", "B15", "B15", "L20", "L20"),
//                        Arrays.asList("D5", "D5", "S10", "B15", "L20")
//                );
//                for (int i = 0; i < 4; i++) {
//                    Player player = game.getPlayers().get(i);
//                    player.getHand().clear();
//                    for (String cardId : initialHands3.get(i)) {
//                        player.getHand().add(createCardFromId(cardId));
//                    }
//                    player.sortHand();
//                }
//                break;
//
//            case 4:
//                List<List<String>> initialHands4 = Arrays.asList(
//                        Arrays.asList("F10", "F15"),
//                        Arrays.asList("D5", "D5"),
//                        Arrays.asList("D5", "D5"),
//                        Arrays.asList("D5", "D5")
//                );
//                for (int i = 0; i < 4; i++) {
//                    Player player = game.getPlayers().get(i);
//                    player.getHand().clear();
//                    for (String cardId : initialHands4.get(i)) {
//                        player.getHand().add(createCardFromId(cardId));
//                    }
//                    player.sortHand();
//                }
//                break;
//
//        }
//    }
//
//    private void provideInput(String input) {
//        inputStream = new ByteArrayInputStream(input.getBytes());
//        game.setScanner(new Scanner(inputStream));
//    }
//
//    @When("P{int} sets up the quest stages:")
//    public void setupQuestStages(int playerNum, DataTable dataTable) {
//// Initialize remaining cards count for this player
//        remainingCards.putIfAbsent(playerNum, new HashMap<>());
//        Map<String, Integer> cardCounts = remainingCards.get(playerNum);
//        cardCounts.clear();
//
//// Count initial cards in hand
//        Player player = game.getPlayers().get(playerNum - 1);
//        for (Card card : player.getHand()) {
//            cardCounts.merge(card.getId(), 1, Integer::sum);
//        }
//
//// Get or create the list of used card indices for this player
//        List<Integer> usedIndices = usedCardIndices.computeIfAbsent(playerNum, k -> new ArrayList<>());
//// Get or create the list of discarded cards for this player
//        List<Card> discardedCards = discardedCardsByPlayer.computeIfAbsent(playerNum, k -> new ArrayList<>());
//
//        List<Map<String, String>> stages = dataTable.asMaps(String.class, String.class);
//        for (Map<String, String> stage : stages) {
//            String[] cards = stage.get("cards").split(",");
//            for (String card : cards) {
//                String trimmedCard = card.trim();
//                int index = findNextAvailableIndex(player, trimmedCard, usedIndices);
//                usedIndices.add(index);
//                simulatedInput += index + "\n";
//// Decrease remaining count for this card
//                cardCounts.put(trimmedCard, cardCounts.get(trimmedCard) - 1);
//// Add the used card to the discarded list
//                discardedCards.add(player.getHand().remove(index));
//// Remove the used card from the player's hand
//            }
//            simulatedInput += "done\n";
//        }
//
//// Add the discarded cards back to the player's hand
//        player.getHand().addAll(discardedCards);
//        player.sortHand();
//
//        provideInput(simulatedInput);
//
//    }
//
//    private int findNextAvailableIndex(Player player, String cardId, List<Integer> usedIndices) {
//        List<Card> hand = player.getHand();
//        List<Integer> availableIndices = IntStream.range(0, hand.size())
//                .filter(i -> hand.get(i).getId().equals(cardId))
//                .boxed()
//                .toList();
//
//        int targetIndex = availableIndices.getFirst();
//        int adjustment = 0;
//        for (int usedIndex : usedIndices) {
//            if (usedIndex < targetIndex && hand.get(usedIndex).getId().equals(cardId)) {
//                adjustment++;
//            }
//        }
//
//        return targetIndex - adjustment;
//    }
//
//    private void initializeCardCountsForPlayer(int playerNum) {
//        Player player = game.getPlayers().get(playerNum - 1);
//        Map<String, Integer> cardCounts = new HashMap<>();
//
//// Count initial cards in hand
//        for (Card card : player.getHand()) {
//            cardCounts.merge(card.getId(), 1, Integer::sum);
//        }
//
//        remainingCards.put(playerNum, cardCounts);
//    }
//
//    private AdventureCard createCardFromId(String id) {
//        char type = id.charAt(0);
//        int value = Integer.parseInt(id.substring(1));
//        return new AdventureCard(id, type == 'F' ? CardType.FOE : CardType.WEAPON, value);
//    }
//
//    @Then("P{int} should have {int} shields")
//    public void verifyShields(int playerNum, int expectedShields) {
//        Player player = game.getPlayers().get(playerNum - 1);
//        assertEquals(expectedShields, player.getShields());
//    }
//
//    @Then("P{int}'s hand should contain")
//    public void verifyPlayerHand(int playerNum, DataTable dataTable) {
//        Player player = game.getPlayers().get(playerNum - 1);
//        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
//
//        List<String> expectedCardIds = rows.stream()
//                .map(row -> row.get("cardId"))
//                .sorted()
//                .collect(Collectors.toList());
//
//        List<String> actualCardIds = player.getHand().stream()
//                .map(Card::getId)
//                .sorted()
//                .collect(Collectors.toList());
//
//        assertEquals(
//                expectedCardIds,
//                actualCardIds,
//                STR."Player \{playerNum}'s hand does not match expected cards"
//        );
//    }
//
//    @Then("P{int}'s hand should have {int} cards")
//    public void verifyPlayerHand(int playerNum, int numberOfCards) {
//        Player player = game.getPlayers().get(playerNum - 1);
//        assertEquals(numberOfCards, player.getHand().size());
//    }
//}