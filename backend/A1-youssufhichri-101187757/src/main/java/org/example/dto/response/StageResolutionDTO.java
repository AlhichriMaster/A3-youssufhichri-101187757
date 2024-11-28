package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.Player;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StageResolutionDTO {
    private int stageNumber;
    private List<Player> winners;
}
