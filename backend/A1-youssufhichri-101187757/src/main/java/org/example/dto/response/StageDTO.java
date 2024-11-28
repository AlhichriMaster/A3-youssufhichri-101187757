package org.example.dto.response;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StageDTO {
    private int stageNumber;
    private CardDTO foeCard;
    private List<CardDTO> weaponCards;
    private int totalValue;
}