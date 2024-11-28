package org.example.dto.request;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class SetupQuestRequest {
    private String sponsorId;
    private List<StageSetupRequest> stages;
}
