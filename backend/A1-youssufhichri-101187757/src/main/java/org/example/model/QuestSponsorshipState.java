package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestSponsorshipState {
    private List<String> potentialSponsors; // Players who CAN sponsor
    private List<String> declinedSponsors;  // Players who said no
    private String currentSponsorBeingAsked; // Current player being asked

    public QuestSponsorshipState(List<String> potentialSponsors) {
        this.potentialSponsors = potentialSponsors;
        this.declinedSponsors = new ArrayList<>();
        this.currentSponsorBeingAsked = potentialSponsors.isEmpty() ? null : potentialSponsors.get(0);
    }

    public String getNextPotentialSponsor() {
        return potentialSponsors.stream()
                .filter(id -> !declinedSponsors.contains(id))
                .findFirst()
                .orElse(null);
    }

    public void addDeclinedSponsor(String playerId) {
        declinedSponsors.add(playerId);
        // Update who's being asked next
        currentSponsorBeingAsked = getNextPotentialSponsor();
    }

    public boolean hasMorePotentialSponsors() {
        return declinedSponsors.size() < potentialSponsors.size();
    }
}
