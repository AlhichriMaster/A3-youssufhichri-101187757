package org.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Component
@NoArgsConstructor
public class Deck {
    private List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        cards.add(card);
    }

    public Card drawCard() {
        if (isEmpty()) {
            return null;
        }
//        System.out.println("We got into drawCard");
        return cards.remove(cards.size() -1);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void refillFromDiscardPile(Deck discardPile) {
        this.cards.addAll(discardPile.getCards());
        discardPile.getCards().clear();
        this.shuffle();
    }

    public void addFirst(Card card) {
        cards.add(0, card);
    }

    public void addLast(Card card) {
        cards.add(cards.size()-1, card);
    }
}
