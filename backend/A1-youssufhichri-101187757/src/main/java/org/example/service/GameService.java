package org.example.service;

import jdk.jfr.Event;
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
    private final PlayerService playerService;
    private final QuestService questService;

    @Autowired
    public GameService(PlayerService playerService, QuestService questService) {
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
        game.setPendingQuest(null);
        game.setCurrentQuest(null);
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
//        System.out.println("This is the event card that we pulled: " + card.getId());
//        handleEventCard(game, card);
        return createGameStateDTO(game);
    }

    // Event handling
    public GameStateDTO handleEventCard(Game game, EventCard card) {
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
        }
        return createGameStateDTO(game);
    }

    private void handleQueensFavor(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
//        System.out.println("Before: Current player: " + currentPlayer.getId() + " Current hand size: " + currentPlayer.getHand().size());
        drawAdventureCards(currentPlayer, game.getAdventureDeck(), 2);
        checkHandSize(game, currentPlayer);
    }

    private void handleProsperity(Game game) {
        game.getPlayers().forEach(player -> {
            drawAdventureCards(player, game.getAdventureDeck(), 2);
            checkHandSize(game, player);
        });
    }

    private void checkHandSize(Game game, Player player) {
        if (player.getHand().size() > 12) {

            game.setGameStatus(GameStatus.HAND_TRIM_REQUIRED);

            game.setPlayerNeedingTrim(player.getId());  // New field in Game
        }
    }

    private void drawAdventureCards(Player player, Deck deck, int count) {
        for (int i = 0; i < count; i++) {
            player.drawCard(deck);
        }
    }

    public GameStateDTO playTurn(Game game) {
        if (game.getGameStatus() == GameStatus.FINISHED) {
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
//            moveToNextPlayer(game);
            currentTurn++;
        }
        return createGameStateDTO(game);
    }

    public GameStateDTO addShieldsToWinners(Game game, List<String> playerIds) {
        for (String playerId : playerIds) {
            Player winner = game.getPlayers().stream()
                    .filter(player -> player.getId().equals(playerId))
                    .findFirst()
                    .orElse(null);

            if (winner != null) {
                // Add shields equal to the number of stages in the quest
                System.out.println("This is the players shields before we add: " + winner);
                System.out.println(winner.addShields(game.getCurrentQuest().getStages()));

                // Check for game over immediately after adding shields
                if (winner.getShields() >= 7) {
                    game.setGameStatus(GameStatus.FINISHED);
                    return createGameStateDTO(game);
                }
            }
        }

        return createGameStateDTO(game);
    }

    public GameStateDTO handleQuestCompletion(Game game) {
        Quest currentQuest = game.getCurrentQuest();
        if (currentQuest != null) {
            questService.rewardSponsor(game, currentQuest);

            // After rewarding the sponsor, check if they need to trim their hand
            Player sponsor = currentQuest.getSponsor();
            if (sponsor.getHand().size() > 12) {
                game.setGameStatus(GameStatus.HAND_TRIM_REQUIRED);
                game.setPlayerNeedingTrim(sponsor.getId());
            }
        }
        return createGameStateDTO(game);
    }


    // Helper method to create GameStateDTO
    public GameStateDTO createGameStateDTO(Game game) {
        GameStatus status = game.getGameStatus();
        if (isGameOver(game)) {
            status = GameStatus.FINISHED;
        }

        return new GameStateDTO(
                game.getPlayers().stream()
                        .map(playerService::convertToPlayerDTO)
                        .collect(Collectors.toList()),
                game.getCurrentPlayer().getId(),
                status,  // Use our determined status
                game.getAdventureDeck().getCards().size(),
                game.getEventDeck().getCards().size(),
                game.getCurrentQuest() != null ? questService.convertToQuestDTO(game.getCurrentQuest()) : null,
                game.getPendingQuest(),
                game.getQuestSponsorshipState(),
                game.getCurrentSponsor()
        );
    }
}