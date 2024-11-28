package org.example.service;

import org.example.dto.enums.CardType;
import org.example.dto.enums.EventType;
import org.example.model.AdventureCard;
import org.example.model.Deck;
import org.example.model.EventCard;
import org.springframework.stereotype.Service;

@Service
public class DeckService {

    // Method to create a fresh adventure deck with all cards
    public Deck createAdventureDeck() {
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

    public Deck createEventDeck() {
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
}