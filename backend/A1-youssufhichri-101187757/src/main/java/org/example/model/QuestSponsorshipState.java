package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestSponsorshipState {
    private List<String> remainingPlayers;

    public void removePlayer(String playerId) {
        remainingPlayers.remove(playerId);
    }
}
