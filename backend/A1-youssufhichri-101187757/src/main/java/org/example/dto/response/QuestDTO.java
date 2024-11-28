package org.example.dto.response;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import org.example.dto.enums.QuestStatus;

@Data
@AllArgsConstructor
public class QuestDTO {
    private int stages;
    private String sponsorId;
    private List<StageDTO> stageDetails;
    private List<String> potentialParticipants;
    private QuestStatus status;
}