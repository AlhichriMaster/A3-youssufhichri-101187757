package org.example.service;

import lombok.Data;
import org.example.dto.enums.CardType;
import org.example.dto.enums.EventType;
import org.example.model.AdventureCard;
import org.example.model.Card;
import org.example.model.Deck;
import org.example.model.EventCard;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Data
public class DeckService {

    private boolean testMode = false;
    private String testScenario = "ONE_WINNER"; // Default test scenario

    // Method to create appropriate adventure deck based on test scenario
    public Deck createAdventureDeck() {
        if (testMode) {
            System.out.println("Creating test deck for scenario: " + testScenario);
            return switch (testScenario) {
                case "TWO_WINNER" -> {
                    System.out.println("We got into 2 winner instead of 1");
                    yield create2WinnerAdventureDeck();
                }
                case "ONE_WINNER" -> {
                    System.out.println("We got into 1 correctly");
                    yield create1WinnerAdventureDeck();
                }
                case "ZERO_WINNER" ->{
                    yield create0WinnerAdventureDeck();
                }
                case "A1_SCENARIO" ->{
                    yield createA1ScenarioAdventureDeck();
                }
                default -> createNormalAdventureDeck();
            };
        }
        return createNormalAdventureDeck();
    }

    // Method to create appropriate event deck based on test scenario
    public Deck createEventDeck() {
        if (testMode) {
            System.out.println("Creating test event deck for scenario: " + testScenario);
            return switch (testScenario) {
                case "TWO_WINNER" -> {
                    System.out.println("We got into 2 winner instead of 1");
                    yield createTwoWinnerEventDeck();
                }
                case "ONE_WINNER" -> {
                    System.out.println("We got into 1 correctly");
                    yield createOneWinnerEventDeck();
                }
                case "ZERO_WINNER" -> {
                    yield createZeroWinnerEventDeck();
                }
                case "A1_SCENARIO" ->{
                    yield createA1ScenarioEventDeck();
                }
                default -> createNormalEventDeck();
            };
        }
        return createNormalEventDeck();
    }

    // One Winner Adventure Deck
    private Deck create1WinnerAdventureDeck() {
        Deck deck = new Deck();
        List<Card> orderedCards = new ArrayList<>();

        // P1's initial hand
        for (int i = 0; i < 2; i++) {
            orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
            orderedCards.add(new AdventureCard("F10", CardType.FOE, 10));
            orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
            orderedCards.add(new AdventureCard("F20", CardType.FOE, 20));
        }
        for (int i = 0; i < 4; i++) {
            orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));
        }

        // P2's initial hand
        orderedCards.add(new AdventureCard("F25", CardType.FOE, 25));
        orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));
        addWeaponSet(orderedCards, 2, 3, 2, 2, 1); // 2H, 3S, 2B, 2L, 1E

        // P3's initial hand
        orderedCards.add(new AdventureCard("F25", CardType.FOE, 25));
        orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));
        addWeaponSet(orderedCards, 2, 3, 2, 2, 1); // 2H, 3S, 2B, 2L, 1E

        // P4's initial hand
        orderedCards.add(new AdventureCard("F25", CardType.FOE, 25));
        orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));
        orderedCards.add(new AdventureCard("F70", CardType.FOE, 70));
        addWeaponSet(orderedCards, 2, 3, 2, 2, 0); // 2H, 3S, 2B, 2L

        // Cards to be drawn during quest stages
        addQuestDrawCards(orderedCards);

        // Add all cards to deck in reverse order
        for (int i = orderedCards.size() - 1; i >= 0; i--) {
            deck.addCard(orderedCards.get(i));
        }

        return deck;
    }

    // One Winner Event Deck
    private Deck createOneWinnerEventDeck() {
        Deck deck = new Deck();

        // Add events in specific order
        deck.addCard(new EventCard("Q3", EventType.QUEST));            // Final quest
        deck.addCard(new EventCard("Queen's Favor", EventType.QUEENS_FAVOR));
        deck.addCard(new EventCard("Prosperity", EventType.PROSPERITY));
        deck.addCard(new EventCard("Plague", EventType.PLAGUE));
        deck.addCard(new EventCard("Q4", EventType.QUEST));            // First quest

        return deck;
    }

    // Helper methods for card creation
    private void addWeaponSet(List<Card> cards, int horses, int swords, int axes, int lances, int excalibur) {
        for (int i = 0; i < horses; i++) cards.add(new AdventureCard("H10", CardType.WEAPON, 10));
        for (int i = 0; i < swords; i++) cards.add(new AdventureCard("S10", CardType.WEAPON, 10));
        for (int i = 0; i < axes; i++) cards.add(new AdventureCard("B15", CardType.WEAPON, 15));
        for (int i = 0; i < lances; i++) cards.add(new AdventureCard("L20", CardType.WEAPON, 20));
        for (int i = 0; i < excalibur; i++) cards.add(new AdventureCard("E30", CardType.WEAPON, 30));
    }

    private void addQuestDrawCards(List<Card> cards) {
        // First quest draws
        cards.add(new AdventureCard("F5", CardType.FOE, 5));
        cards.add(new AdventureCard("F10", CardType.FOE, 10));
        cards.add(new AdventureCard("F20", CardType.FOE, 20));

        //stage 2 draws
        cards.add(new AdventureCard("F15", CardType.FOE, 15));
        cards.add(new AdventureCard("F5", CardType.FOE, 5));
        cards.add(new AdventureCard("F25", CardType.FOE, 25));

        //stage 3
        cards.add(new AdventureCard("F5", CardType.FOE, 5));
        cards.add(new AdventureCard("F10", CardType.FOE, 10));
        cards.add(new AdventureCard("F20", CardType.FOE, 20));

        //stage 4
        cards.add(new AdventureCard("F5", CardType.FOE, 5));
        cards.add(new AdventureCard("F10", CardType.FOE, 10));
        cards.add(new AdventureCard("F20", CardType.FOE, 20));


        //P1 draws since he sponsored
        cards.add(new AdventureCard("F5", CardType.FOE, 5));
        cards.add(new AdventureCard("F5", CardType.FOE, 5));

        cards.add(new AdventureCard("F10", CardType.FOE, 10));
        cards.add(new AdventureCard("F10", CardType.FOE, 10));

        cards.add(new AdventureCard("F15", CardType.FOE, 15));
        cards.add(new AdventureCard("F15", CardType.FOE, 15));
        cards.add(new AdventureCard("F15", CardType.FOE, 15));
        cards.add(new AdventureCard("F15", CardType.FOE, 15));


        // Prosperity draws
        //P1
        cards.add(new AdventureCard("F25", CardType.FOE, 25));
        cards.add(new AdventureCard("F25", CardType.FOE, 25));
        //P2
        cards.add(new AdventureCard("H10", CardType.WEAPON, 10));
        cards.add(new AdventureCard("S10", CardType.WEAPON, 10));
        //P3
        cards.add(new AdventureCard("B15", CardType.WEAPON, 15));
        cards.add(new AdventureCard("F40", CardType.FOE, 40));
        //P4
        cards.add(new AdventureCard("D5", CardType.WEAPON, 5));
        cards.add(new AdventureCard("D5", CardType.WEAPON, 5));

        // Queen's favor draws
        cards.add(new AdventureCard("F30", CardType.FOE, 30));
        cards.add(new AdventureCard("F25", CardType.FOE, 25));


        // Final quest draws
        cards.add(new AdventureCard("B15", CardType.WEAPON, 15));
        cards.add(new AdventureCard("H10", CardType.WEAPON, 10));
        cards.add(new AdventureCard("F50", CardType.FOE, 50));

        //stage 2
        cards.add(new AdventureCard("S10", CardType.WEAPON, 10));
        cards.add(new AdventureCard("S10", CardType.WEAPON, 10));


        //stage 3
        cards.add(new AdventureCard("F40", CardType.FOE, 40));
        cards.add(new AdventureCard("F50", CardType.FOE, 50));

        //final draw for P1 cus he sponsored the quest
        cards.add(new AdventureCard("H10", CardType.WEAPON, 10));
        cards.add(new AdventureCard("H10", CardType.WEAPON, 10));
        cards.add(new AdventureCard("H10", CardType.WEAPON, 10));

        cards.add(new AdventureCard("S10", CardType.WEAPON, 10));
        cards.add(new AdventureCard("S10", CardType.WEAPON, 10));
        cards.add(new AdventureCard("S10", CardType.WEAPON, 10));
        cards.add(new AdventureCard("S10", CardType.WEAPON, 10));

        cards.add(new AdventureCard("F35", CardType.FOE, 35));




    }


    // Method to create a fresh adventure deck with all cards
    public Deck createNormalAdventureDeck() {
        Deck deck = new Deck();

        // Add Foe cards
        addFoeCards(deck, 8, 5);
        addFoeCards(deck, 7, 10);
        addFoeCards(deck, 8, 15);
        addFoeCards(deck, 7, 20);
        addFoeCards(deck, 7, 25);
        addFoeCards(deck, 4, 30);
        addFoeCards(deck, 4, 35);
        addFoeCards(deck, 2, 40);
        addFoeCards(deck, 2, 50);
        addFoeCards(deck, 1, 70);

        // Add Weapon cards
        addWeaponCards(deck, 6, "D", 5);
        addWeaponCards(deck, 12, "H", 10);
        addWeaponCards(deck, 16, "S", 10);
        addWeaponCards(deck, 8, "B", 15);
        addWeaponCards(deck, 6, "L", 20);
        addWeaponCards(deck, 2, "E", 30);

        deck.shuffle();
        return deck;
    }

    public Deck createNormalEventDeck() {
        Deck deck = new Deck();

        // Add Quest cards
        addQuestCards(deck, 3, 2);
        addQuestCards(deck, 4, 3);
        addQuestCards(deck, 3, 4);
        addQuestCards(deck, 2, 5);

        // Add Event cards
        deck.addCard(new EventCard("Plague", EventType.PLAGUE));
        addEventCards(deck, 2, "Queen's Favor", EventType.QUEENS_FAVOR);
        addEventCards(deck, 2, "Prosperity", EventType.PROSPERITY);

        deck.shuffle();
        return deck;
    }

    private void addFoeCards(Deck deck, int count, int value) {
        for (int i = 0; i < count; i++) {
            deck.addCard(new AdventureCard("F" + value, CardType.FOE, value));
        }
    }

    private void addWeaponCards(Deck deck, int count, String prefix, int value) {
        for (int i = 0; i < count; i++) {
            deck.addCard(new AdventureCard(prefix + value, CardType.WEAPON, value));
        }
    }

    private void addQuestCards(Deck deck, int count, int stages) {
        for (int i = 0; i < count; i++) {
            deck.addCard(new EventCard("Q" + stages, EventType.QUEST));
        }
    }

    private void addEventCards(Deck deck, int count, String name, EventType type) {
        for (int i = 0; i < count; i++) {
            deck.addCard(new EventCard(name, type));
        }
    }

    public Deck createEmptyDeck() {
        return new Deck();
    }


    /////////////2Winner_quest////////////
    private Deck create2WinnerAdventureDeck() {
        Deck deck = new Deck();
        List<Card> orderedCards = new ArrayList<>();  // We'll add cards in exact order

        // First 48 cards are the initial hands in exact order (12 cards × 4 players)

        // P1's initial hand
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F10", CardType.FOE, 10));
        orderedCards.add(new AdventureCard("F10", CardType.FOE, 10));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));  // dagger
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse 1
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse 2
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe 1
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe 2
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // lance

        // P2's initial hand
        orderedCards.add(new AdventureCard("F40", CardType.FOE, 40));
        orderedCards.add(new AdventureCard("F50", CardType.FOE, 50));
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse 1
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse 2
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword 1
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword 2
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword 3
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe 1
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe 2
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // lance 1
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // lance 2
        orderedCards.add(new AdventureCard("E30", CardType.WEAPON, 30)); // excalibur

        // P3's initial hand
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));  // F5 x4
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));  // dagger x3
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse x5
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10));
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10));
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10));
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10));

        // P4's initial hand
        orderedCards.add(new AdventureCard("F50", CardType.FOE, 50));
        orderedCards.add(new AdventureCard("F70", CardType.FOE, 70));
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse x2
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10));
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword x3
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10));
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10));
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe x2
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15));
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // lance x2
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20));
        orderedCards.add(new AdventureCard("E30", CardType.WEAPON, 30)); // excalibur

        // Cards to be drawn during stage 1
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));     // P2 draws and discards
        orderedCards.add(new AdventureCard("F40", CardType.FOE, 40));   // P3 draws and discards F5
        orderedCards.add(new AdventureCard("F10", CardType.FOE, 10));   // P4 draws and discards F10

        // Stage 2 draws
        orderedCards.add(new AdventureCard("F10", CardType.FOE, 10));   // P2 draws
        orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));   // P4 draws

        // Stage 3 draws
        orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));   // P2 draws
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));   // P4 draws

        // Stage 4 draws
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));   // P2 draws
        orderedCards.add(new AdventureCard("F20", CardType.FOE, 20));   // P4 draws

        // P1's 11 card draw after quest
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F10", CardType.FOE, 10));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("F20", CardType.FOE, 20));
        orderedCards.add(new AdventureCard("F20", CardType.FOE, 20));
        orderedCards.add(new AdventureCard("F20", CardType.FOE, 20));
        orderedCards.add(new AdventureCard("F20", CardType.FOE, 20));
        orderedCards.add(new AdventureCard("F25", CardType.FOE, 25));
        orderedCards.add(new AdventureCard("F25", CardType.FOE, 25));
        orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));

        //Second quest cards to be drawn:

        //stage 1
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));

        //stage 2
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));

        //stage 3
        orderedCards.add(new AdventureCard("F25", CardType.FOE, 25));
        orderedCards.add(new AdventureCard("F25", CardType.FOE, 25));

        //P3 Draws for sponsoring the quest
        orderedCards.add(new AdventureCard("F20", CardType.FOE, 20));
        orderedCards.add(new AdventureCard("F20", CardType.FOE, 20));
        orderedCards.add(new AdventureCard("F25", CardType.FOE, 25));
        orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10));
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15));
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15));
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20));


        // Add all cards to deck in order
        for (int i = orderedCards.size() - 1; i >= 0; i--) {
            deck.addCard(orderedCards.get(i));
        }

        return deck;
    }

    private Deck createTwoWinnerEventDeck() {
        Deck deck = new Deck();

        // Add exact event cards in order
        deck.addCard(new EventCard("Q3", EventType.QUEST));    // Second quest (3 stages)
        deck.addCard(new EventCard("Q4", EventType.QUEST));    // First quest (4 stages)

        return deck;
    }




    private Deck create0WinnerAdventureDeck() {
        Deck deck = new Deck();
        List<Card> orderedCards = new ArrayList<>();

        // P1's initial hand
        orderedCards.add(new AdventureCard("F50", CardType.FOE, 50));
        orderedCards.add(new AdventureCard("F70", CardType.FOE, 70));
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5)); // dagger x2
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse x2
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10));
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword x2
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10));
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe x2
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15));
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // lance x2
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20));

        // P2's initial hand
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F10", CardType.FOE, 10));
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F20", CardType.FOE, 20));
        orderedCards.add(new AdventureCard("F25", CardType.FOE, 25));
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));
        orderedCards.add(new AdventureCard("F40", CardType.FOE, 40));
        orderedCards.add(new AdventureCard("E30", CardType.WEAPON, 30)); // excalibur

        // P3's initial hand - same pattern as P2 but with lance instead of excalibur
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F10", CardType.FOE, 10));
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F20", CardType.FOE, 20));
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F25", CardType.FOE, 25));
        orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));
        orderedCards.add(new AdventureCard("F40", CardType.FOE, 40));
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // lance

        // P4's initial hand - similar pattern but with excalibur
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F10", CardType.FOE, 10));
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F20", CardType.FOE, 20));
        for (int i = 0; i < 2; i++) orderedCards.add(new AdventureCard("F25", CardType.FOE, 25));
        orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));
        orderedCards.add(new AdventureCard("F50", CardType.FOE, 50));
        orderedCards.add(new AdventureCard("E30", CardType.WEAPON, 30)); // excalibur



        // Cards to be drawn during quest stages
        //stage 1
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));   // P2 draws and discards
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15)); // P3 draws and discards
        orderedCards.add(new AdventureCard("F10", CardType.FOE, 10)); // P4 draws and discards

        // P1's draw after quest
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F10", CardType.FOE, 10));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        for (int i = 0; i < 4; i++) orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));
        for (int i = 0; i < 4; i++) orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10));
        for (int i = 0; i < 3; i++) orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10));

        // Add all cards to deck in reverse order
        for (int i = orderedCards.size() - 1; i >= 0; i--) {
            deck.addCard(orderedCards.get(i));
        }

        return deck;
    }

    private Deck createZeroWinnerEventDeck() {
        Deck deck = new Deck();

        // Add events in specific order
        deck.addCard(new EventCard("Q2", EventType.QUEST)); // 2-stage quest

        return deck;
    }


    private Deck createA1ScenarioAdventureDeck() {
        Deck deck = new Deck();
        List<Card> orderedCards = new ArrayList<>();

        // P1's initial hand (will be randomly dealt but we need these exact cards)
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));  // dagger
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // lance

        // P2's initial hand
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("F40", CardType.FOE, 40));
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));  // dagger
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // lance

        // P3's initial hand
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));  // dagger
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // lance

        // P4's initial hand
        orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("F15", CardType.FOE, 15));
        orderedCards.add(new AdventureCard("F40", CardType.FOE, 40));
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));  // dagger
        orderedCards.add(new AdventureCard("D5", CardType.WEAPON, 5));  // dagger
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // sword
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse
        orderedCards.add(new AdventureCard("H10", CardType.WEAPON, 10)); // horse
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // axe
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // lance
        orderedCards.add(new AdventureCard("E30", CardType.WEAPON, 30)); // excalibur

        // Cards to be drawn during quest stages
        // Stage 1 draws
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // P3 draws sword
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // P4 draws axe
        orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));   // P1 draws

        // Stage 2 draws
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // P3 draws lance
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // P4 draws lance
        orderedCards.add(new AdventureCard("F10", CardType.FOE, 10));   // P1 draws

        // Stage 3 draws
        orderedCards.add(new AdventureCard("B15", CardType.WEAPON, 15)); // P3 draws axe
        orderedCards.add(new AdventureCard("S10", CardType.WEAPON, 10)); // P4 draws sword

        // Stage 4 draws
        orderedCards.add(new AdventureCard("F30", CardType.FOE, 30));   // P3 draws
        orderedCards.add(new AdventureCard("L20", CardType.WEAPON, 20)); // P4 draws lance

        // P2's random draw cards after sponsoring (13 cards)
        for (int i = 0; i < 13; i++) {
            orderedCards.add(new AdventureCard("F5", CardType.FOE, 5));
        }

        // Add all cards to deck in reverse order
        for (int i = orderedCards.size() - 1; i >= 0; i--) {
            deck.addCard(orderedCards.get(i));
        }

        return deck;
    }

    private Deck createA1ScenarioEventDeck() {
        Deck deck = new Deck();

        // Add exact event cards in order
        deck.addCard(new EventCard("Q4", EventType.QUEST));    // 4-stage quest

        return deck;
    }

}