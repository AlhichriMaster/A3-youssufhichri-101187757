package org.example.service;

import lombok.Data;
import org.example.dto.enums.CardType;
import org.example.dto.enums.QuestStatus;
import org.example.dto.response.*;
import org.example.dto.request.*;
import org.example.model.*;
import org.example.exception.GameException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.example.dto.enums.QuestStatus;

@Data
@Service
public class QuestService {
    private final PlayerService playerService;
    private List<String> questParticipants = new ArrayList<>();

    @Autowired
    public QuestService(PlayerService playerService) {
        this.playerService = playerService;
    }

    // Update the QuestDTO constructor call
    public QuestDTO handleQuestCard(Game game, EventCard card) {
        int stages = Integer.parseInt(card.getId().substring(1));
        List<String> potentialSponsors = game.getPlayers().stream()
                .filter(player -> canSponsorQuest(player, stages))  // Pass stages instead of Quest
                .map(Player::getId)
                .collect(Collectors.toList());

        if (potentialSponsors.isEmpty()) {
            return null;
        }

        game.setPendingQuest(card);
        game.setQuestSponsorshipState(new QuestSponsorshipState(potentialSponsors));

        return new QuestDTO(
                stages,
                null,
                new ArrayList<>(),
                potentialSponsors,
                QuestStatus.AWAITING_SPONSOR
        );
    }

    public boolean canSponsorQuest(Player player, int stages) {
        int foeCount = 0;
        int totalValue = 0;

        for (Card card : player.getHand()) {
            if (card instanceof AdventureCard) {
                AdventureCard adventureCard = (AdventureCard) card;
                if (adventureCard.getType() == CardType.FOE) {
                    foeCount++;
                }
                totalValue += adventureCard.getValue();
            }
        }
        return foeCount >= stages && totalValue >= (stages * (stages + 1) / 2) * 5;
    }

    public boolean setupQuest(Game game, String playerId) {
        // First check if there's a pending quest card
        EventCard pendingQuest = game.getPendingQuest();
        if (pendingQuest == null || !pendingQuest.getType().toString().equals("QUEST")) {
            throw new GameException("No pending quest");
        }

        // Get the player who wants to sponsor
        Player sponsor = game.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new GameException("Player not found"));

        // Create quest from pending quest card
        int stages = Integer.parseInt(pendingQuest.getId().substring(1));
        Quest quest = new Quest(stages);

        // Check if player can sponsor
        if (!canSponsorQuest(sponsor, stages)) {
            return false;
        }

        // Set up the quest
        quest.setSponsor(sponsor);
        game.setCurrentQuest(quest);

//        System.out.println("We set the current quest to: " + game.getCurrentQuest().getStages());
        return true;
    }

    // Stage setup with validation
    public Stage setupStage(Game game, SetupStageRequest request) {
        Quest currentQuest = game.getCurrentQuest();
        Player sponsor = currentQuest.getSponsor();
//        System.out.println("Cuurent player, also sponsor: " + sponsor.getId());
        // Create new stage
        Stage stage = new Stage();

        // Validate and process each card in the request
        for (String cardId : request.getCardIds()) {
//            System.out.println("Card we are trying to get: " + cardId);
            Card card = findCardInHand(sponsor, cardId);
            if (card == null) {
                throw new GameException("Card not found in sponsor's hand");
            }

            if (card instanceof AdventureCard) {
                AdventureCard adventureCard = (AdventureCard) card;

                // Validate foe card (only one allowed per stage)
                if (adventureCard.getType() == CardType.FOE) {
                    if (stage.getFoeCard() != null) {
                        throw new GameException("Stage already has a foe card");
                    }
                    stage.addCard(adventureCard);
                    sponsor.discardCard(card);
                }
                // Validate weapon card (no duplicates allowed)
                else if (adventureCard.getType() == CardType.WEAPON) {
                    if (stage.hasWeapon(adventureCard.getId())) {
                        throw new GameException("Duplicate weapon card not allowed");
                    }
                    stage.addCard(adventureCard);
                    sponsor.discardCard(card);
                }
            }
        }

        // Validate stage completion
        if (!stage.isValid()) {
            throw new GameException("Stage must have exactly one foe card");
        }

        // Validate stage value compared to previous stage
        if (!currentQuest.getStageList().isEmpty()) {
            Stage previousStage = currentQuest.getStage(currentQuest.getStageList().size() - 1);
            if (stage.getValue() <= previousStage.getValue()) {
                throw new GameException("Stage value must be greater than previous stage");
            }
        }

        // Add valid stage to quest
        currentQuest.addStage(stage);

        return stage;
    }





    public List<String> findPotentialParticipants(Game game) {
        return game.getPlayers().stream()
                .filter(player -> player != game.getCurrentQuest().getSponsor()
                        && canParticipateInQuest(player, game.getCurrentQuest()))
                .map(Player::getId)
                .collect(Collectors.toList());
    }


    public boolean participateInQuest(Game game, String playerId) {
        Player player = findPlayerById(game, playerId);
        Quest currentQuest = game.getCurrentQuest();

        if (currentQuest == null) {
            throw new GameException("No active quest");
        }

        if (player == currentQuest.getSponsor()) {
            throw new GameException("Sponsor cannot participate in their own quest");
        }

        if (!canParticipateInQuest(player, currentQuest)) {
            return false;
        }

        questParticipants.add(playerId);
        return true;
    }

    public boolean canParticipateInQuest(Player player, Quest quest) {
        int weaponCount = 0;
        for (Card card : player.getHand()) {
            if (card instanceof AdventureCard) {
                AdventureCard adventureCard = (AdventureCard) card;
                if (adventureCard.getType() == CardType.WEAPON) {
                    weaponCount++;
                }
            }
        }
        // Player needs at least one weapon card per stage
        return weaponCount >= quest.getStages();
    }

    public AttackResult processAttack(Game game, AttackRequest request) {
        Player player = findPlayerById(game, request.getPlayerId());
//        System.out.println("Player Hand: " + player.getHand().size());
//        for(Card card : player.getHand()){
//            System.out.println(card.getId());
//        }
        Quest quest = game.getCurrentQuest();
        Stage currentStage = quest.getStage(request.getStageNumber() - 1);

        // Validate player is a participant
        if (!questParticipants.contains(request.getPlayerId())) {
            throw new GameException("Player is not a quest participant");
        }

        // Get and validate attack cards
        List<AdventureCard> attackCards = new ArrayList<>();
        for (String cardId : request.getCardIds()) {
            System.out.println(cardId);
            Card card = findCardInHand(player, cardId);
            if (!(card instanceof AdventureCard) ||
                    ((AdventureCard) card).getType() != CardType.WEAPON) {
                throw new GameException("Invalid card for attack: " + cardId);
            }
            attackCards.add((AdventureCard) card);
        }

        // Calculate attack value
        int attackValue = calculateAttackValue(attackCards);

        // Determine if stage is cleared
        boolean stageCleared = attackValue >= currentStage.getValue();

        // Remove used cards from player's hand
        attackCards.forEach(card -> player.discardCard(card));

        // Draw a card after attack
        player.drawCard(game.getAdventureDeck());

        // If stage was cleared, check if it was the last stage
        if (stageCleared && request.getStageNumber() == quest.getStages()) {
            player.addShields(quest.getStages());
        }

        return new AttackResult(
                true,
                attackValue,
                stageCleared,
                attackCards.stream().map(Card::getId).collect(Collectors.toList())
        );
    }


    private QuestDTO createQuestDTO(Game game, Quest quest, QuestResolutionDTO resolution) {
        return new QuestDTO(
                quest.getStages(),
                quest.getSponsor() != null ? quest.getSponsor().getId() : null,
                convertStagesToDTO(quest.getStageList()),
                resolution != null ?
                        new ArrayList<>() :
                        findPotentialParticipants(game),
                determineQuestStatus(resolution)
        );
    }

    // Add this helper method to determine quest status based on resolution
    private QuestStatus determineQuestStatus(QuestResolutionDTO resolution) {
        if (resolution == null) {
            return QuestStatus.IN_PROGRESS;
        }
        return QuestStatus.COMPLETED;
    }

    private int calculateAttackValue(List<AdventureCard> cards) {
        return cards.stream()
                .mapToInt(AdventureCard::getValue)
                .sum();
    }

    private void discardCards(Game game, List<Card> cards) {
        cards.forEach(card -> game.getAdventureDiscardPile().addCard(card));
    }

    public QuestDTO convertToQuestDTO(Quest quest) {
        return new QuestDTO(
                quest.getStages(),
                quest.getSponsor() != null ? quest.getSponsor().getId() : null,
                convertStagesToDTO(quest.getStageList()),
                new ArrayList<>(),
                determineQuestStatus(quest)
        );
    }


    // Add these methods:
    private Stage createStage(Player sponsor, StageSetupRequest stageRequest) {
        Stage stage = new Stage();
        // Add foe card
//        AdventureCard foeCard = sponsor.getHand().remove(stageRequest.getFoeCardId());
        AdventureCard foeCard = findCardInHand(sponsor, stageRequest.getFoeCardId());
        if (foeCard.getType() != CardType.FOE) {
            throw new GameException("Card must be a foe card");
        }
        stage.setFoeCard(foeCard);

        // Add weapon cards
        for (String weaponId : stageRequest.getWeaponCardIds()) {
            AdventureCard weaponCard = findCardInHand(sponsor, weaponId);
            if (weaponCard.getType() != CardType.WEAPON) {
                throw new GameException("Card must be a weapon card");
            }
            stage.addCard(weaponCard);
        }
        return stage;
    }

    private AdventureCard findCardInHand(Player sponsor, String foeCardId) {
//        System.out.println("We got into findCardInHand");
        List<Card> hand = sponsor.getHand();
        for (int i = 0; i < hand.size(); i++){
//            System.out.println("This is the hand being looped on in findCardInHand: " + hand.get(i).getId());
//            System.out.println("This is the card id that we got from the frontend: " + foeCardId);
            if(hand.get(i).getId().equals(foeCardId)){
                AdventureCard adventureCard = (AdventureCard) hand.get(i);
//                System.out.println("Successfully found the card and returned + removed it");
                return adventureCard;
            }
        }
        throw new GameException("Card does not exist in your hand. SHOULD NEVER HAPPEN");
    }


    private boolean isValidStage(Stage stage, Quest quest, int stageIndex) {
        if (!stage.isValid()) {
            return false;
        }
        if (stageIndex > 0) {
            Stage previousStage = quest.getStage(stageIndex - 1);
            return stage.getValue() > previousStage.getValue();
        }
        return true;
    }

    private Player findPlayerById(Game game, String playerId) {
        return game.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new GameException("Player not found: " + playerId));
    }

    private List<StageDTO> convertStagesToDTO(List<Stage> stages) {
        List<StageDTO> stageDTOs = new ArrayList<>();
        for (int i = 0; i < stages.size(); i++) {
            stageDTOs.add(convertStageToDTO(stages.get(i), i + 1));
        }
        return stageDTOs;
    }

    private CardDTO convertToCardDTO(AdventureCard card) {
        return new CardDTO(
                card.getId(),
                card.getType().toString(),
                card.getValue()
        );
    }

    private StageDTO convertStageToDTO(Stage stage, int stageNumber) {
        return new StageDTO(
                stageNumber,
                convertToCardDTO(stage.getFoeCard()),
                stage.getWeaponCards().stream()
                        .map(this::convertToCardDTO)
                        .collect(Collectors.toList()),
                stage.getValue()
        );
    }

    private StageResolutionDTO resolveStage(Game game, List<Player> participants, int stageNum) {
        Stage stage = game.getCurrentQuest().getStage(stageNum);
        List<Player> winners = new ArrayList<>();

        for (Player player : participants) {
            player.drawCard(game.getAdventureDeck());
        }

        // Return stage resolution
        return new StageResolutionDTO(
                stageNum,
                winners
        );
    }

    private void awardSponsorCards(Game game, Player sponsor, int cardCount) {
        for (int i = 0; i < cardCount; i++) {
            sponsor.drawCard(game.getAdventureDeck());
        }
    }

    private int calculateSponsorCards(Quest quest) {
        return quest.getStages() * 2; // Or whatever your game rules specify
    }

    private QuestStatus determineQuestStatus(Quest quest) {
        if (quest.getSponsor() == null) {
            return QuestStatus.AWAITING_SPONSOR;
        }
        // Add other status conditions based on your game rules
        return QuestStatus.IN_PROGRESS;
    }
}