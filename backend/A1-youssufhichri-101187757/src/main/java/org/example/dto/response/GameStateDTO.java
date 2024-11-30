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
    private List<PlayerDTO> players;
    private String currentPlayerId;
    private QuestDTO currentQuest;
    private GameStatus gameStatus;
    private int adventureDeckSize;
    private int eventDeckSize;
    private EventCard pendingQuest;  // Add this
    private QuestSponsorshipState questSponsorshipState;  // Add this
    private String currentSponsor;  // Add this
}
