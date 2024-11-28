package org.example.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.dto.enums.EventType;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventCard extends Card {
    private EventType type;

    public EventCard(String id, EventType type) {
        super(id);
        this.type = type;
    }
}