package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetupStageRequest {
    private String playerId;
    private int stageNumber;
    private List<String> cardIds;
}
