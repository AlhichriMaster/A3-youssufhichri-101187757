package org.example;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Player {
    private String id;
    private List<Card> hand;
    private int shields;

    public Player(String pId){
        this.id = pId;
        this.hand = new ArrayList<Card>();
        int shields = 0;
    }

    //functions

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getShields() {
        return shields;
    }

    public void addShields(int amount) {
        shields += amount;
        System.out.println(id + " gained " + amount + " shields. Total: " + shields);
    }

    public void removeShields(int amount) {
        shields = Math.max(0, shields - amount);
        System.out.println(id + " lost " + amount + " shields. Total: " + shields);
    }

    public Card drawCard(Deck deck) {
        Card drawnCard = deck.drawCard();
        if (drawnCard != null) {
            hand.add(drawnCard);
        }
        sortHand();
        return drawnCard;
    }

    public void trimHand(Scanner scanner) {
        while (hand.size() > 12) {
            System.out.println(id + ", your hand has " + hand.size() + " cards. You need to discard " + (hand.size() - 12) + " card(s).");
            for (int i = 0; i < hand.size(); i++) {
                System.out.println(i + ": " + hand.get(i).getId());
            }
            System.out.println("Enter the index of the card you want to discard:");

            try {
                int index = Integer.parseInt(scanner.nextLine().trim());
                if (index >= 0 && index < hand.size()) {
                    Card discarded = hand.remove(index);
                    System.out.println("Discarded: " + discarded.getId());
                } else {
                    System.out.println("Invalid index. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        System.out.println("Your hand has been trimmed to 12 cards.");
        for (int i = 0; i < hand.size(); i++) {
            System.out.println(i + ": " + hand.get(i).getId());
        }
        sortHand();
    }

    public void discardCard(Card card){
        if (!hand.remove(card)) {
            throw new IllegalArgumentException("Card not in hand: " + card.getId());
        }
    }


    public void sortHand(){
        List<Card> sortedHand = new ArrayList<>();

        //First, sort and add Foes
        List<Card> sortedFoes = hand.stream()
                .filter(card -> card instanceof AdventureCard && ((AdventureCard) card).getType() == CardType.FOE)
                .sorted(Comparator.comparingInt(card -> ((AdventureCard) card).getValue()))
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
        sortedHand.addAll(sortedWeapons);


        //Replace our hand with the new sorted list
        this.hand = sortedHand;
    }

}
