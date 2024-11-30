package org.example.service;

import org.example.dto.enums.GameStatus;
import org.example.dto.response.GameStateDTO;
import org.example.dto.response.WinnerDTO;
import org.example.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final DeckService deckService;
    private final PlayerService playerService;
    private final QuestService questService;

    @Autowired
    public GameService(DeckService deckService, PlayerService playerService, QuestService questService) {
        this.deckService = deckService;
        this.playerService = playerService;
        this.questService = questService;
    }

    // Game state checks and updates
    public boolean isGameOver(Game game) {
        return game.getPlayers().stream().anyMatch(player -> player.getShields() >= 7);
    }

    public GameStateDTO moveToNextPlayer(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        int currentIndex = game.getPlayers().indexOf(currentPlayer);
        Player nextPlayer = game.getPlayers().get((currentIndex + 1) % game.getPlayers().size());
        game.setCurrentPlayer(nextPlayer);
        return createGameStateDTO(game);
    }

    // Card handling
    public GameStateDTO drawEventCard(Game game) {
        Deck eventDeck = game.getEventDeck();
        if (eventDeck.isEmpty()) {
            if (game.getEventDiscardPile().isEmpty()) {
                // Handle case where both decks are empty
                return createGameStateDTO(game);
            }
            eventDeck.refillFromDiscardPile(game.getEventDiscardPile());
        }
        EventCard card = (EventCard) eventDeck.drawCard();
        game.setPendingQuest(card);
        System.out.println("This is the event card that we pulled: " + card.getId());
//        handleEventCard(game, card);
        return createGameStateDTO(game);
    }

    // Event handling
    private void handleEventCard(Game game, EventCard card) {
        switch (card.getType()) {
            case PLAGUE:
                game.getCurrentPlayer().removeShields(2);
                break;
            case QUEENS_FAVOR:
                handleQueensFavor(game);
                break;
            case PROSPERITY:
                handleProsperity(game);
                break;
            case QUEST:
                // Instead of immediately processing the quest, set it as pending
                game.setPendingQuest(card);
                game.setQuestSponsorshipState(new QuestSponsorshipState(
                        game.getPlayers().stream()
                                .map(Player::getId)
                                .collect(Collectors.toList())
                ));
                break;
        }
    }

    private void handleQueensFavor(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        drawAdventureCards(currentPlayer, game.getAdventureDeck(), 2);
    }

    private void handleProsperity(Game game) {
        game.getPlayers().forEach(player ->
                drawAdventureCards(player, game.getAdventureDeck(), 2)
        );
    }

    private void drawAdventureCards(Player player, Deck deck, int count) {
        for (int i = 0; i < count; i++) {
            player.drawCard(deck);
        }
    }

    public List<WinnerDTO> getWinners(Game game) {
        return game.getPlayers().stream()
                .filter(player -> player.getShields() >= 7)
                .map(player -> new WinnerDTO(player.getId(), player.getShields()))
                .collect(Collectors.toList());
    }

    public GameStateDTO playTurn(Game game) {
        if (isGameOver(game)) {
            return createGameStateDTO(game);
        }

        drawEventCard(game);
        return createGameStateDTO(game);
    }

    public GameStateDTO startGame(Game game) {
        int maxTurns = 100; // Or some reasonable number
        int currentTurn = 0;
        while (!isGameOver(game) && currentTurn < maxTurns) {
            playTurn(game);
            moveToNextPlayer(game);
            currentTurn++;
        }
        return createGameStateDTO(game);
    }

    // Helper method to create GameStateDTO
    public GameStateDTO createGameStateDTO(Game game) {
        return new GameStateDTO(
                game.getPlayers().stream()
                        .map(playerService::convertToPlayerDTO)
                        .collect(Collectors.toList()),
                game.getCurrentPlayer().getId(),
                game.getCurrentQuest() != null ? questService.convertToQuestDTO(game.getCurrentQuest()) : null,
                isGameOver(game) ? GameStatus.FINISHED : GameStatus.IN_PROGRESS,
                game.getAdventureDeck().getCards().size(),
                game.getEventDeck().getCards().size(),
                game.getPendingQuest(),  // Add these
                game.getQuestSponsorshipState(),
                game.getCurrentSponsor()
        );
    }
}