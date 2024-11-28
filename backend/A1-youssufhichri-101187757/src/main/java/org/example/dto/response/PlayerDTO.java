package org.example.dto.response;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class PlayerDTO {
    private String id;
    private int shields;
    private List<CardDTO> hand;
    private boolean isCurrentTurn;
}
