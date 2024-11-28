package org.example.dto.enums;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum QuestStatus {
    AWAITING_SPONSOR,
    AWAITING_SETUP,
    AWAITING_PARTICIPANTS,
    IN_PROGRESS,
    COMPLETED
}
