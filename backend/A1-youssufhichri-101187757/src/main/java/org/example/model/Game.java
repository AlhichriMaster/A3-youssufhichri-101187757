package org.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.service.DeckService;
import org.example.service.PlayerService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.ArrayList;

@Data
@Service
@NoArgsConstructor // Add this if you need a default constructor
public class Game {
    private List<Player> players = new ArrayList<>();
    private Deck adventureDeck;
    private Deck eventDeck;
    private Player currentPlayer;
    private Deck eventDiscardPile;
    private Quest currentQuest;
    private Deck adventureDiscardPile;

    private DeckService deckService;
    private PlayerService playerService;

    @Autowired
    public Game(DeckService deckService, PlayerService playerService) {
        this.deckService = deckService;
        this.playerService = playerService;
    }

    @PostConstruct
    public void setupGame() {
        initializeDecks();
        createPlayers();
        initializePlayersHands();
        currentPlayer = players.get(0);
    }

    private void initializeDecks() {
        adventureDeck = deckService.createAdventureDeck();
        eventDeck = deckService.createEventDeck();
        adventureDiscardPile = deckService.createEmptyDeck();
        eventDiscardPile = deckService.createEmptyDeck();
    }

    private void createPlayers() {
        players = playerService.createPlayers(4);
    }

    private void initializePlayersHands() {
        playerService.initializeAllPlayersHands(players, adventureDeck);
    }
}