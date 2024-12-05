package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AttackResult {
    private boolean success;
    private int attackValue;
    private boolean stageCleared;
    private List<String> cardsUsed;
}
