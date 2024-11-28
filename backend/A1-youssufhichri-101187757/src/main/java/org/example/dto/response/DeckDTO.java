package org.example.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
public class DeckDTO {
    private int numberOfCards;
    private boolean isEmpty;
    private List<CardDTO> visibleCards;  // For discard piles or other visible cards
}