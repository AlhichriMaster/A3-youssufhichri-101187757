package org.example.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class DiscardCardRequest {
    private String playerId;
    private List<String> cardIds;
}
