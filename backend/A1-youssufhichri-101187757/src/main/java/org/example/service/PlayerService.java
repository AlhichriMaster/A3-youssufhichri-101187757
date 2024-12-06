package org.example.service;

import org.example.dto.request.DiscardCardRequest;
import org.example.dto.response.PlayerDTO;
import org.example.dto.response.CardDTO;
import org.example.dto.response.HandDTO;
import org.example.model.*;
import org.springframework.stereotype.Service;
import org.example.exception.GameException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    public Player createPlayer(String playerId) {
        return new Player(playerId);
    }

    public List<Player> createPlayers(int numberOfPlayers) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= numberOfPlayers; i++) {
            players.add(createPlayer("P" + i));
        }
        return players;
    }

    public void initializePlayerHand(Player player, Deck deck) {
        for (int i = 0; i < 12; i++) {
            player.drawCard(deck);
        }
        player.sortHand();
    }

    public void initializeAllPlayersHands(List<Player> players, Deck deck) {
        for (Player player : players) {
            initializePlayerHand(player, deck);
        }
    }

    // Convert Player to PlayerDTO
    public PlayerDTO convertToPlayerDTO(Player player) {
        return new PlayerDTO(
                player.getId(),
                player.getShields(),
                convertToCardDTOs(player.getHand()),
                false  // isCurrentTurn will be set by GameService
        );
    }

    // Convert a list of Cards to CardDTOs
    private List<CardDTO> convertToCardDTOs(List<Card> cards) {
        return cards.stream()
                .map(this::convertToCardDTO)
                .collect(Collectors.toList());
    }


    private CardDTO convertToCardDTO(Card card) {
        if (card instanceof AdventureCard) {
            AdventureCard adventureCard = (AdventureCard) card;
            return new CardDTO(
                    card.getId(),
                    adventureCard.getType().toString(),
                    adventureCard.getValue()
            );
        } else if (card instanceof EventCard) {
            EventCard eventCard = (EventCard) card;
            return new CardDTO(
                    card.getId(),
                    eventCard.getType().toString(),
                    0  // Event cards don't have value
            );
        } else {
            // Default case or throw exception
            return new CardDTO(
                    card.getId(),
                    "UNKNOWN",
                    0
            );
        }
    }

    public void discardCardsFromHand(Game game, DiscardCardRequest request) {

        Player player = game.getPlayers().stream()
                .filter(p -> p.getId().equals(request.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new GameException("Player not found"));

        // Discard the selected cards
        for (String cardId : request.getCardIds()) {
            Card cardToDiscard = player.getHand().stream()
                    .filter(card -> card.getId().equals(cardId))
                    .findFirst()
                    .orElseThrow(() -> new GameException("Card not found in player's hand"));

            System.out.println("we are ACTUALLY discarding these cards: " + cardToDiscard);
            player.discardCard(cardToDiscard);
        }
    }
}