package org.example;

import java.util.ArrayList;
import java.util.List;

public class Stage {
    private AdventureCard foeCard;
    private List<AdventureCard> weaponCards;

    public Stage() {
        this.weaponCards = new ArrayList<>();
    }


    public List<AdventureCard> getWeaponCards() {
        return weaponCards;
    }

    public void setWeaponCards(List<AdventureCard> weaponCards) {
        this.weaponCards = weaponCards;
    }

    public AdventureCard getFoeCard() {
        return foeCard;
    }

    public void setFoeCard(AdventureCard foeCard) {
        this.foeCard = foeCard;
    }




    public void addCard(AdventureCard card) {
        if (card.getType() == CardType.FOE) {
            if (foeCard == null) {
                foeCard = card;
            } else {
                throw new IllegalStateException("Stage already has a foe card");
            }
        } else if (card.getType() == CardType.WEAPON) {
            weaponCards.add(card);
        }
    }

    public int getValue() {
        int value = foeCard != null ? foeCard.getValue() : 0;
        for (AdventureCard weapon : weaponCards) {
            value += weapon.getValue();
        }
        return value;
    }

    public boolean isValid() {
        return foeCard != null;
    }
}
