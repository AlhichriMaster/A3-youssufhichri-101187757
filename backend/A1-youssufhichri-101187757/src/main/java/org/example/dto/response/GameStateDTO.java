package org.example.dto.response;

import java.util.List;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.dto.enums.GameStatus;
import org.example.model.EventCard;
import org.example.model.QuestSponsorshipState;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameStateDTO {
    //Essential Game state
    private List<PlayerDTO> players;
    private String currentPlayerId;
    private GameStatus gameStatus;
    //Deck information
    private int adventureDeckSize;
    private int eventDeckSize;
    //Quest related fields
    private QuestDTO currentQuest;
    private EventCard pendingQuest;  // Add this
    private QuestSponsorshipState questSponsorshipState;  // Add this
    private String currentSponsor;  // Add this
}
