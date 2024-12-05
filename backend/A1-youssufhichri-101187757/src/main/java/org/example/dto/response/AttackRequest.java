package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AttackRequest {
    private String playerId;
    private List<String> cardIds;
    private int stageNumber;
}
