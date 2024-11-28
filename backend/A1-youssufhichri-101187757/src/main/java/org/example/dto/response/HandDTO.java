package org.example.dto.response;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class HandDTO {
    private String playerId;
    private List<CardDTO> cards;
}
