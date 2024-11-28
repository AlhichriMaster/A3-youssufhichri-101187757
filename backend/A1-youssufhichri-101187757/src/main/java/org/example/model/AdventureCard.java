package org.example.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.example.dto.enums.CardType;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdventureCard extends Card {
    private CardType type;
    private int value;

    // Keep the constructor since we're extending Card
    public AdventureCard(String id, CardType type, int value) {
        super(id);
        this.type = type;
        this.value = value;
    }
}