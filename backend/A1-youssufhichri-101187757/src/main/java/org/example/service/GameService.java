package org.example.service;

import jdk.jfr.Event;
import org.example.dto.enums.GameStatus;
import org.example.dto.response.GameStateDTO;
import org.example.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.example.exception.GameException;
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
        System.out.println("We are in drawEventCard");
        Deck eventDeck = game.getEventDeck();
        System.out.println("We got the event deck");
        if (eventDeck.isEmpty()) {
            System.out.println("We got in the first if");
            if (game.getEventDiscardPile().isEmpty()) {
                // Handle case where both decks are empty
                System.out.println("We got in the second if");
                return createGameStateDTO(game);
            }
            eventDeck.refillFromDiscardPile(game.getEventDiscardPile());
        }

        System.out.println("We got out of the ifs");
        EventCard card = (EventCard) eventDeck.drawCard();
        System.out.println("We got a card" + card.getId());
        game.setPendingQuest(card);
        System.out.println("This is the event card that we pulled: " + card.getId());
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

    public GameStateDTO drawCardForQuestParticipation(Game game, String playerId){
        Player player = game.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new GameException("Player not found"));

        System.out.println("We drew this card : " + player.drawCard(game.getAdventureDeck()) + " For this player: " + player.getId());

        return createGameStateDTO(game);
    }

    public GameStateDTO playTurn(Game game) {
//        System.out.println("We got into play");
        if (game.getGameStatus() == GameStatus.FINISHED) {
            System.out.println("We got into the if statement inside of playTurn meaning we set the game as FINISHED");
            return createGameStateDTO(game);
        }
        System.out.println("We called play turn at the end meaning we clicked the drawcard button");
        return drawEventCard(game);
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
        // Process each winner one at a time
        for (String playerId : playerIds) {
            Player winner = game.getPlayers().stream()
                    .filter(player -> player.getId().equals(playerId))
                    .findFirst()
                    .orElse(null);

            if (winner != null) {
                // Log before state
                System.out.println("This is the players shields before we add: " + winner);

                // Add shields and store result
                int addedShields = game.getCurrentQuest().getStages();
                winner.addShields(addedShields);

                // Log after state to verify addition
                System.out.println(winner.getId() + " gained: " + addedShields +
                        "shields. Current total: " + winner.getShields());
            }
        }

        // After processing all winners, check game state
        if (isGameOver(game)) {
            game.setGameStatus(GameStatus.FINISHED);
        }

        // Return updated state
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
                game.setCurrentQuest(null);
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