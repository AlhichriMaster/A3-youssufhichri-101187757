package org.example;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Game {

    //Members
    private List<Player> players;
    private Deck adventureDeck;
    private Deck eventDeck;

    private Player currentPlayer;
    private Deck eventDiscardPile;
    private Quest currentQuest;
    private Deck adventureDiscardPile;

    private Scanner scanner = new Scanner(System.in);

    //Constructors

    public Game() {
        players = new ArrayList<>();
        adventureDeck = new Deck();
        eventDeck = new Deck();
        adventureDiscardPile = new Deck();
        eventDiscardPile = new Deck();
        scanner = new Scanner(System.in);
    }

    public void setupGame() {
        initializeAdventureDeck();
        initializeEventDeck();
        createPlayers();
        initializePlayersHands();
        currentPlayer = players.get(0);
    }

    private void createPlayers() {
        for (int i = 1; i <= 4; i++) {
            players.add(new Player("P" + i));
        }
    }


    //functions
    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }


    public Deck getAdventureDeck() {
        return adventureDeck;
    }

    public void setAdventureDeck(Deck adventureDeck) {
        this.adventureDeck = adventureDeck;
    }

    public Deck getEventDeck() {
        return eventDeck;
    }

    public void setEventDeck(Deck eventDeck) {
        this.eventDeck = eventDeck;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Deck getEventDiscardPile() {
        return eventDiscardPile;
    }

    public void setEventDiscardPile(Deck eventDiscardPile) {
        this.eventDiscardPile = eventDiscardPile;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public Quest getCurrentQuest() {
        return currentQuest;
    }

    public void setCurrentQuest(Quest currentQuest) {
        this.currentQuest = currentQuest;
    }

    private void initializeAdventureDeck() {
        adventureDeck = new Deck();

        // Add Foe cards
        addFoeCards(8, 5);
        addFoeCards(7, 10);
        addFoeCards(8, 15);
        addFoeCards(7, 20);
        addFoeCards(7, 25);
        addFoeCards(4, 30);
        addFoeCards(4, 35);
        addFoeCards(2, 40);
        addFoeCards(2, 50);
        addFoeCards(1, 70);

        // Add Weapon cards
        addWeaponCards(6, "D", 5);  // Daggers
        addWeaponCards(12, "H", 10);  // Horses
        addWeaponCards(16, "S", 10);  // Swords
        addWeaponCards(8, "B", 15);  // Battle-axes
        addWeaponCards(6, "L", 20);  // Lances
        addWeaponCards(2, "E", 30);  // Excaliburs

        adventureDeck.shuffle();
    }

    private void addFoeCards(int count, int value) {
        for (int i = 0; i < count; i++) {
            adventureDeck.addCard(new AdventureCard("F" + value, CardType.FOE, value));
        }
    }

    private void addWeaponCards(int count, String prefix, int value) {
        for (int i = 0; i < count; i++) {
            adventureDeck.addCard(new AdventureCard(prefix + value, CardType.WEAPON, value));
        }
    }


    private void initializeEventDeck() {
        eventDeck = new Deck();

        // Add Quest cards
        addQuestCards(3, 2);
        addQuestCards(4, 3);
        addQuestCards(3, 4);
        addQuestCards(2, 5);

        // Add Event cards
        eventDeck.addCard(new EventCard("Plague", EventType.PLAGUE));
        addEventCards(2, "Queen's Favor", EventType.QUEENS_FAVOR);
        addEventCards(2, "Prosperity", EventType.PROSPERITY);

        eventDeck.shuffle();
    }

    private void addQuestCards(int count, int stages) {
        for (int i = 0; i < count; i++) {
            eventDeck.addCard(new EventCard("Q" + stages, EventType.QUEST));
        }
    }

    private void addEventCards(int count, String name, EventType type) {
        for (int i = 0; i < count; i++) {
            eventDeck.addCard(new EventCard(name, type));
        }
    }


    public void initializePlayersHands() {
        for (Player player : players) {
            for (int i = 0; i < 12; i++) {
                player.drawCard(adventureDeck); // Assuming 'adventureDeck' is available in Game class
            }
            player.sortHand();
        }
    }

    public void displayPlayerHand(Player player) {
        System.out.println("Current player's hand:");
        int i = 0;
        for(Card card : player.getHand()){
            System.out.println(i + ": " + card.getId());
            i++;
        }
    }

    public void moveToNextPlayer() {
        endPlayerTurn();
        int currentIndex = players.indexOf(currentPlayer);
        currentPlayer = players.get((currentIndex + 1) % players.size());
        startPlayerTurn();
    }


    public EventCard drawEventCard() {
        if (eventDeck.isEmpty()) {
            eventDeck.refillFromDiscardPile(eventDiscardPile);
        }
        EventCard card = (EventCard) eventDeck.drawCard();
        System.out.println(currentPlayer.getId() + " drew event card: " + card.getId());
        return card;
    };


    public void handleEventCard(EventCard card){
        switch (card.getType()) {
            case PLAGUE:
                //current player loses 2 shield
                currentPlayer.removeShields(2);
                break;

            case QUEENS_FAVOR:
                //current player gets to draw 2 adventure cards
                currentPlayer.drawCard(adventureDeck);
                currentPlayer.drawCard(adventureDeck);
                currentPlayer.trimHand(scanner);
                break;

            case PROSPERITY:
                //all players get to draw 2 adventure cards
                for (Player player : players){
                    player.drawCard(adventureDeck);
                    player.drawCard(adventureDeck);
                    player.trimHand(scanner);
                    flushDisplay();
                }
                break;
            case QUEST:
                handleQuestCard(card);
                break;
        }
    }

    private void handleQuestCard(EventCard card) {
        int stages = Integer.parseInt(card.getId().substring(1));
        currentQuest = new Quest(stages);
        Player sponsor = findSponsor();
        if (sponsor != null) {
            currentQuest.setSponsor(sponsor);
            int cardsToDraw = setupQuest(sponsor);
            List<Player> participants = findParticipants();
            resolveQuest(participants);

            //quest is resolved and sponsor needs to draw cards
            // Quest sponsor draws cards after quest ends
            cardsToDraw = cardsToDraw + currentQuest.getStages();
            for (int i = 0; i < cardsToDraw; i++) {
                sponsor.drawCard(adventureDeck);
            }
            sponsor.trimHand(scanner);
            System.out.println(sponsor.getId() + " drew " + cardsToDraw + " cards as the quest sponsor.");
        }
    }


    private Player findSponsor() {
        int currentIndex = players.indexOf(currentPlayer);

        for (int i = 0; i < players.size(); i++) {
            // Use modulo to wrap around the player list
            Player player = players.get((currentIndex + i) % players.size());
            if(canSponsorQuest(player)){
                System.out.println(player.getId() + ", do you want to sponsor this quest? (y/n)");
                String response = scanner.nextLine().trim().toLowerCase();
                if (response.equals("y")) {
                    return player;
                }
            }
        }
        System.out.println("Quest card drawn, but no one can sponsor. Ignoring.");
        return null;
    }

    private boolean canSponsorQuest(Player player) {
        int stages = currentQuest.getStages();
        int foeCount = 0;
        int totalValue = 0;

        for (Card card : player.getHand()) {
            if (card instanceof AdventureCard) {
                AdventureCard adventureCard = (AdventureCard) card;
                if (adventureCard.getType() == CardType.FOE) {
                    foeCount++;
                }
                totalValue += adventureCard.getValue();
            }
        }
        // Check if player has enough foes and total card value for all stages
        return foeCount >= stages && totalValue >= (stages * (stages + 1) / 2) * 5;
    }


    public int setupQuest(Player sponsor) {
        int cardsUsedInQuest = 0;
//        System.out.println(currentPlayer.getHand());
        for (int i = 1; i <= currentQuest.getStages(); i++) {
            Stage stage = new Stage();
            while (true) {
                System.out.println("Setting up stage " + i);
                displayPlayerHand(sponsor);
                System.out.println("Enter card index to add to stage, or 'done' to finish stage:");
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("done")) {
                    if (stage.isValid() && (i == 1 || stage.getValue() > currentQuest.getStage(i - 2).getValue())) {
                        System.out.println("Successfully built stage " + i);
                        System.out.println("Cards used for this stage: ");
                        System.out.println("Selected Foe: " + stage.getFoeCard().getId());

                        for(int j = 0; j < stage.getWeaponCards().size(); j++){
                            if(j == 0){
                                System.out.print("Selected Weapon Cards: ");
                            }
                            System.out.println(stage.getWeaponCards().get(j).getId() + " ");
                        }
                        //if we reach here, it means stage was done properly. We add 1 cus there can only be 1 foe per stage, and we add weapon card size
                        cardsUsedInQuest = cardsUsedInQuest + 1 + stage.getWeaponCards().size();
                        break;
                    } else {
                        System.out.println("Invalid stage. It must have a foe and be stronger than the previous stage.");
                    }
                } else {
                    try {
                        int index = Integer.parseInt(input);
                        Card card = sponsor.getHand().get(index);
                        if (card instanceof AdventureCard) {
                            stage.addCard((AdventureCard) card);
                            sponsor.discardCard(card);
                        } else {
                            System.out.println("Invalid card. Only adventure cards can be used in quests.");
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid: Stage already has a foe card. Pick a weapon or type 'done'");
                    }
                }
            }
            currentQuest.addStage(stage);
        }
        return cardsUsedInQuest;
    }

    public List<Player> findParticipants(){
        List<Player> participants = new ArrayList<>();
        for(Player player : players){
            if(player != currentQuest.getSponsor() && canParticipate(player)){
                System.out.println(player.getId() + ", do you want to participate in this quest? (y/n)");
                String response = scanner.nextLine().trim().toLowerCase();
                if(response.equals("y")){
                    participants.add(player);
                }
            }
        }
        return participants;
    }

    private boolean canParticipate(Player player) {
        int stages = currentQuest.getStages();
        int weaponCount = 0;

        for (Card card : player.getHand()) {
            if (card instanceof AdventureCard) {
                AdventureCard adventureCard = (AdventureCard) card;
                if (adventureCard.getType() == CardType.WEAPON) {
                    weaponCount++;
                    if (weaponCount >= stages){
                        return true;
                    }
                }
            }
        }
        // Check if player has enough foes and total card value for all stages
        return false;
    }

    private void resolveQuest(List<Player> participants) {
        int cardsUsedInQuest = 0;


        for (int i = 0; i < currentQuest.getStages(); i++) {
            Stage stage = currentQuest.getStage(i);
            List<Player> stageWinners = new ArrayList<>();

            // Participants draw 1 card and trim their hand
            for (Player player : participants) {
                player.drawCard(adventureDeck);
                System.out.println(player.getId() + " drew a card");
                player.trimHand(scanner);
            }

            for (Player player : participants) {
                if (offerWithdrawal(player, i + 1)) {
                    continue; // Player withdrew, move to next player
                }
                List<Card> attack = setupAttack(player);
                int attackValue = calculateAttackValue(attack);
                if (attackValue >= stage.getValue()) {
                    stageWinners.add(player);
                    System.out.println(player.getId() + " passed stage " + (i + 1));
                } else {
                    System.out.println(player.getId() + " failed stage " + (i + 1));
                }
                discardCards(attack);
            }
            participants = stageWinners;
            if (participants.isEmpty()) {
                break;
            }
        }
        for (Player winner : participants) {
            winner.addShields(currentQuest.getStages());
        }
    }

    private boolean offerWithdrawal(Player player, int stageNumber){
        System.out.println(player.getId() + ", do you want to continue to stage " + stageNumber + " or withdraw? (c/w)");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("w")) {
            System.out.println(player.getId() + " has withdrawn from the quest.");
            return true;
        }
        return false;
    }


    private void discardCards(List<Card> cards) {
        for (Card card : cards) {
            adventureDiscardPile.addCard(card);
        }
    }

    public int calculateAttackValue(List<Card> attack) {
        return attack.stream()
                .mapToInt(card -> ((AdventureCard) card).getValue())
                .sum();
    }



    public List<Card> setupAttack(Player player) {
        List<Card> attack = new ArrayList<>();
        while (true) {
            System.out.println(player.getId() + ", set up your attack");
            displayPlayerHand(player);
            System.out.println("Enter card index to add to attack, or 'done' to finish:");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("done")) {
                break;
            } else {
                try {
                    int index = Integer.parseInt(input);
                    Card card = player.getHand().get(index);
                    if (card instanceof AdventureCard && ((AdventureCard) card).getType() == CardType.WEAPON) {
                        attack.add(card);
                        player.getHand().remove(card);
                    } else {
                        System.out.println("Invalid card. Try again.");
                    }
                } catch (Exception e) {
                    System.out.println("Invalid input. Try again.");
                }
            }
        }
        System.out.println("Successfully built attack for this stage");

        for(int j = 0; j < attack.size(); j++){
            if(j == 0){
                System.out.print("Weapon Cards used in this attack: ");
            }
            System.out.print(attack.get(j).getId() + " ");
        }
        return attack;
    }



    public void flushDisplay() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
        System.out.println("Screen flushed. Press Enter to continue...");
        scanner.nextLine();
    }

    public void endPlayerTurn() {
        System.out.println(currentPlayer.getId() + ", your turn is over. Press Enter to leave the hot seat.");
        scanner.nextLine();
        flushDisplay();
    }

    public void startPlayerTurn() {
        System.out.println("It's " + currentPlayer.getId() + "'s turn.");
        System.out.println("Press Enter to view your hand.");
        scanner.nextLine();
        displayPlayerHand(currentPlayer);
    }

    public void playGame() {
        while (!isGameOver()) {
            playTurn();
            moveToNextPlayer();
        }
        announceWinners();
    }

    private void playTurn() {
        System.out.println("It's " + currentPlayer.getId() + "'s turn.");
        EventCard eventCard = drawEventCard();
        handleEventCard(eventCard);
    }

    private boolean isGameOver() {
        return players.stream().anyMatch(player -> player.getShields() >= 7);
    }

    private void announceWinners() {
        List<Player> winners = players.stream()
                .filter(player -> player.getShields() >= 7)
                .collect(Collectors.toList());
        System.out.println("Game Over!");
        for (Player winner : winners) {
            System.out.println(winner.getId() + " wins with " + winner.getShields() + " shields!");
        }
    }

}