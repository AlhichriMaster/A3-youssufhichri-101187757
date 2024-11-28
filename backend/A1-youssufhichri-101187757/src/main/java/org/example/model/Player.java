package org.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dto.enums.CardType;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@Component
@NoArgsConstructor
public class Player {
    private String id;
    private List<Card> hand = new ArrayList<>();
    private int shields = 0;

    public Player(String pId) {
        this.id = pId;
        this.hand = new ArrayList<>();
        this.shields = 0;
    }

    public String addShields(int amount) {
        shields += amount;
        return id + " gained: " + amount + "shields. Total: " + shields;
    }

    public String removeShields(int amount) {
        shields = Math.max(0, shields - amount);
        return id + " lost " + amount + " shields. Total " + shields;
    }

    // This method stays largely the same
    public Card drawCard(Deck deck) {
        Card drawnCard = deck.drawCard();
        if (drawnCard != null) {
            hand.add(drawnCard);
        }
        sortHand();
        return drawnCard;
    }

    // Replace scanner-based trimHand with a method that takes an index
    public String discardCardAtIndex(int index) {
        if (index >= 0 && index < hand.size()) {
            Card discarded = hand.remove(index);
            sortHand();
            return "Discarded: " + discarded.getId();
        }
        throw new IllegalArgumentException("Invalid card index");
    }

    // Keep these methods the same
    public void discardCard(Card card) {
        if (!hand.remove(card)) {
            throw new IllegalArgumentException("Card not in hand");
        }
    }


    public void sortHand(){
        List<Card> sortedHand = new ArrayList<>();

        //First, sort and add Foes
        List<Card> sortedFoes = hand.stream()
                .filter(card -> card instanceof AdventureCard && ((AdventureCard) card).getType() == CardType.FOE)
                .sorted(Comparator.comparingInt(card -> ((AdventureCard) card).getValue()))
                .toList();
        sortedHand.addAll(sortedFoes);


        //Now we sort weapons
        List<Card> sortedWeapons = hand.stream()
                .filter(card -> card instanceof AdventureCard && ((AdventureCard) card).getType() == CardType.WEAPON)
                .sorted((c1, c2) -> {
                    AdventureCard ac1 = (AdventureCard) c1;
                    AdventureCard ac2 = (AdventureCard) c2;
                    if (ac1.getValue() != ac2.getValue()) {
                        return Integer.compare(ac1.getValue(), ac2.getValue());
                    } else {
                        //if theyre equal then sort swords before horses
                        return ac1.getId().startsWith("S") ? -1 : 1;
                    }
                })
                .toList();
        sortedHand.addAll(sortedWeapons);


        //Replace our hand with the new sorted list
        this.hand = sortedHand;
    }

}
