package org.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dto.enums.CardType;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Stage {
    private AdventureCard foeCard;
    private List<AdventureCard> weaponCards = new ArrayList<>();

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

    public int numOfCardsUsed(){
        //we can automatically assume that we have a foe card in this stage since it is enforced
        return weaponCards.size() + 1;
    }
}
