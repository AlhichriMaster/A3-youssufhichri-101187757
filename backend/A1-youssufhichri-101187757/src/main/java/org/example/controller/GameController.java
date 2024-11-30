package org.example.controller;

import org.example.dto.request.SetupQuestRequest;
import org.example.dto.request.SponsorshipResponse;
import org.example.dto.response.GameStateDTO;
import org.example.dto.response.QuestDTO;
import org.example.model.Game;
import org.example.model.QuestSponsorshipState;
import org.example.service.GameService;
import org.example.service.QuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/game")
public class GameController {
    private final Game game;
    private final GameService gameService;
    private final QuestService questService;

    @Autowired
    public GameController(Game game, GameService gameService, QuestService questService) {
        this.game = game;
        this.gameService = gameService;
        this.questService = questService;
    }

    @GetMapping("")
    public GameStateDTO getGameState() {
        return gameService.createGameStateDTO(game);
    }

    @GetMapping("/start")
    public GameStateDTO startGame() {
        return gameService.startGame(game);
    }

    @GetMapping("/playTurn")
    public GameStateDTO playTurn() {
        return gameService.playTurn(game);
    }

    @PostMapping("/respondToSponsorship")
    public GameStateDTO respondToSponsorship(@RequestBody SponsorshipResponse response) {
        if (response.isAccepting()) {
            // For now, just mark that they accepted - don't start quest setup yet
            game.setCurrentSponsor(response.getPlayerId());
            // Clear the sponsorship state since we found a sponsor
            game.setQuestSponsorshipState(null);
            // Leave pendingQuest set so we know we're in setup phase
            // The actual quest setup will need to be handled by a separate endpoint
            return gameService.createGameStateDTO(game);
        } else {
            // They declined - move to next player in line
            QuestSponsorshipState sponsorshipState = game.getQuestSponsorshipState();
            sponsorshipState.removePlayer(response.getPlayerId());

            if (sponsorshipState.getRemainingPlayers().isEmpty()) {
                // No one left to ask - discard the quest card
                game.setPendingQuest(null);
                game.setQuestSponsorshipState(null);
            }
        }
        return gameService.createGameStateDTO(game);
    }

    @PostMapping("/setupQuest")
    public QuestDTO setupQuest(@RequestBody SetupQuestRequest request) {
        return questService.setupQuest(game, request);
    }


}