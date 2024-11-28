package org.example.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class WinnerDTO {
    private String playerId;
    private int shields;
}