package org.example;

public class AdventureCard extends Card {
    private CardType type;
    private int value;

    public AdventureCard(String id, CardType type, int value) {
        super(id);
        this.type = type;
        this.value = value;
    }


    public CardType getType() {
        return type;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
