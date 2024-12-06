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

//    public void setTestMode(boolean testMode) {
//        this.isTestMode = testMode;
//    }

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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
                                //////Rigged Draws/////


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

    private Deck createTestEventDeck() {
        Deck deck = new Deck();

        // Add exact event cards in order
        deck.addCard(new EventCard("Q3", EventType.QUEST));    // Second quest (3 stages)
        deck.addCard(new EventCard("Q4", EventType.QUEST));    // First quest (4 stages)

        return deck;
    }




    ///////////////////////1 winner quest////////////////








    public Deck createAdventureDeck() {
        if (testMode) {
            System.out.println("We used the test deck");
            return create2WinnerAdventureDeck();
        }
        return createNormalAdventureDeck();
    }
    public Deck createEventDeck() {
        if (testMode) {
            System.out.println("We used the test event deck");
            return createTestEventDeck();
        }
        return createNormalEventDeck();
    }



}