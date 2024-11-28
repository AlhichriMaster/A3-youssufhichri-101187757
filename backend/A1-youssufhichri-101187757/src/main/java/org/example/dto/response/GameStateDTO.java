package org.example.dto.response;

import java.util.List;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.dto.enums.GameStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameStateDTO {
    private List<PlayerDTO> players;
    private String currentPlayerId;
    private QuestDTO currentQuest;  // Make sure this is QuestDTO, not String
    private GameStatus status;
    private Integer adventureDeckSize;
    private Integer eventDeckSize;
}
