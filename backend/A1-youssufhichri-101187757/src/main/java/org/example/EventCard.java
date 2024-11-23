package org.example;

public class EventCard extends Card {
    private EventType type;

    public EventCard(String id, EventType type) {
        super(id);
        this.type = type;
    }

    public EventType getType() {
        return type;
    }
}