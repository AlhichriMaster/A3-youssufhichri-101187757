package org.example.dto.request;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
public class StageSetupRequest {
    private String foeCardId;
    private List<String> weaponCardIds;
}
