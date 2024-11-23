package org.example;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();
    }


    public void addCard(Card card){
        cards.add(card);
    }

    public Card drawCard() {
        if (isEmpty()) {
            return null;  // Or throw an exception
        }
        return cards.removeLast();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public void refillFromDiscardPile(Deck discardPile) {
        this.cards.addAll(discardPile.cards);
        discardPile.cards.clear();
        this.shuffle();
    }

    public void addFirst(Card card) {
        cards.addFirst(card);
    }

    public void addLast(Card card) {
        cards.addLast(card);
    }
}
