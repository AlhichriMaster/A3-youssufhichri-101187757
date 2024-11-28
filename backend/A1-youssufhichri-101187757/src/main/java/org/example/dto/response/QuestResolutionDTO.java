package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestResolutionDTO {
    private List<StageResolutionDTO> stageResolutions = new ArrayList<>();

    public void addStageResolution(StageResolutionDTO resolution) {
        stageResolutions.add(resolution);
    }
}
