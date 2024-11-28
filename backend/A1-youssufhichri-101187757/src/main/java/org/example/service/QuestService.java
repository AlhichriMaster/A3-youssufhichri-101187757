package org.example.service;

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

@Service
public class QuestService {
    private final PlayerService playerService;

    @Autowired
    public QuestService(PlayerService playerService) {
        this.playerService = playerService;
    }

    // Update the QuestDTO constructor call
    public QuestDTO handleQuestCard(Game game, EventCard card) {
        int stages = Integer.parseInt(card.getId().substring(1));
        Quest quest = new Quest(stages);
        game.setCurrentQuest(quest);

        return new QuestDTO(
                stages,
                null, // sponsor ID
                new ArrayList<>(), // stage details
                findPotentialSponsors(game),
                QuestStatus.AWAITING_SPONSOR
        );
    }

    public List<String> findPotentialSponsors(Game game) {
        return game.getPlayers().stream()
                .filter(player -> canSponsorQuest(player, game.getCurrentQuest()))
                .map(Player::getId)
                .collect(Collectors.toList());
    }

    public boolean canSponsorQuest(Player player, Quest quest) {
        int stages = quest.getStages();
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

    public QuestDTO setupQuest(Game game, SetupQuestRequest request) {
        Quest quest = game.getCurrentQuest();
        Player sponsor = game.getPlayers().stream()
                .filter(p -> p.getId().equals(request.getSponsorId()))
                .findFirst()
                .orElseThrow(() -> new GameException("Sponsor not found"));

        quest.setSponsor(sponsor);

        // Validate and setup stages
        int cardsToDraw = validateAndSetupStages(quest, sponsor, request.getStages());

        // Find potential participants
        List<String> potentialParticipants = findPotentialParticipants(game);

        return new QuestDTO(
                quest.getStages(),
                sponsor.getId(),
                convertStagesToDTO(quest.getStageList()),
                potentialParticipants,
                QuestStatus.AWAITING_PARTICIPANTS
        );
    }

    private int validateAndSetupStages(Quest quest, Player sponsor, List<StageSetupRequest> stageRequests) {
        int cardsUsed = 0;
        for (int i = 0; i < stageRequests.size(); i++) {
            StageSetupRequest stageRequest = stageRequests.get(i);
            Stage stage = createStage(sponsor, stageRequest);

            if (!isValidStage(stage, quest, i)) {
                throw new GameException("Invalid stage setup");
            }

            quest.addStage(stage);
            cardsUsed += stage.numOfCardsUsed();
        }
        return cardsUsed;
    }

    public List<String> findPotentialParticipants(Game game) {
        return game.getPlayers().stream()
                .filter(player -> player != game.getCurrentQuest().getSponsor()
                        && canParticipateInQuest(player, game.getCurrentQuest()))
                .map(Player::getId)
                .collect(Collectors.toList());
    }

    public boolean canParticipateInQuest(Player player, Quest quest) {
        int stages = quest.getStages();
        int totalValue = 0;

        for (Card card : player.getHand()) {
            if (card instanceof AdventureCard) {
                AdventureCard adventureCard = (AdventureCard) card;
                totalValue += adventureCard.getValue();
            }
        }
        return totalValue >= (stages * (stages + 1) / 2) * 5;
    }

    public QuestDTO resolveQuest(Game game, List<String> participantIds) {
        Quest quest = game.getCurrentQuest();
        List<Player> participants = participantIds.stream()
                .map(id -> findPlayerById(game, id))
                .collect(Collectors.toList());

        QuestResolutionDTO resolution = new QuestResolutionDTO();

        for (int stageNum = 0; stageNum < quest.getStages(); stageNum++) {
            StageResolutionDTO stageResolution = resolveStage(game, participants, stageNum);
            resolution.addStageResolution(stageResolution);

            participants = stageResolution.getWinners();
            if (participants.isEmpty()) break;
        }

        // Award shields to winners
        participants.forEach(player -> player.addShields(quest.getStages()));

        // Draw cards for sponsor
        awardSponsorCards(game, quest.getSponsor(), calculateSponsorCards(quest));

        return createQuestDTO(game, quest, resolution);
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

    private int calculateAttackValue(List<Card> attack) {
        return attack.stream()
                .mapToInt(card -> ((AdventureCard) card).getValue())
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
        List<Card> hand = sponsor.getHand();
        for (int i = 0; i < hand.size(); i++){
            if(hand.get(i).getId().equals(foeCardId)){
                AdventureCard adventureCard = (AdventureCard) hand.remove(i);
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