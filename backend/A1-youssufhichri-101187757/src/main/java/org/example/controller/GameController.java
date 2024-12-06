package org.example.controller;

import org.example.dto.request.DiscardCardRequest;
import org.example.dto.response.*;
import org.example.model.Card;
import org.example.model.Game;
import org.example.model.Stage;
import org.example.service.GameService;
import org.example.service.QuestService;
import org.example.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/game")
public class GameController {
    private final Game game;
    private final GameService gameService;
    private final QuestService questService;
    private final PlayerService playerService;

    @Autowired
    public GameController(Game game, GameService gameService, QuestService questService, PlayerService playerService) {
        this.game = game;
        this.gameService = gameService;
        this.questService = questService;
        this.playerService = playerService;
    }

    @GetMapping("/")
    public String redirectToStartGame() {
        return "redirect:/startgame.html";
    }



    @GetMapping("")
    public GameStateDTO getGameState() {
        return gameService.createGameStateDTO(game);
    }

    //start the game
    @GetMapping("/start")
    public GameStateDTO startGame() {
        return gameService.startGame(game);
    }

    //draw event card
    @GetMapping("/playTurn")
    public GameStateDTO playTurn() {
        return gameService.playTurn(game);
    }

    //handle the plague card case
    @PostMapping("/handlePlague")
    public GameStateDTO handlePlague() {
        return gameService.handleEventCard(game, game.getPendingQuest());
    }

    //handle the queen and prosperity cards
    @PostMapping("/handleQandP")
    public GameStateDTO handleQandP() {
        System.out.println(game.getPendingQuest().getType());
        return gameService.handleEventCard(game, game.getPendingQuest());
    }

    //handle the setup of a quest
    @PostMapping("/quest/sponsor")
    public ResponseEntity<Boolean> sponsorQuest(@RequestParam String playerId) {
//        System.out.println("This is the player who is sponsoring the quest: " + playerId);
//        for(Card card : game.getPlayers().get(1).getHand()){
//            System.out.println(card.getId());
//        }
        return ResponseEntity.ok(questService.setupQuest(game, playerId));
    }

    @PostMapping("/quest/setup-stage")
    public ResponseEntity<Stage> setupStage(@RequestBody SetupStageRequest request) {
        return ResponseEntity.ok(questService.setupStage(game, request));
    }

    //handle the setting up of the attack
    @PostMapping("/quest/participate")
    public ResponseEntity<Boolean> participateInQuest(@RequestParam String playerId) {
//        System.out.println("This is the playerId we got: " + playerId);
        return ResponseEntity.ok(questService.participateInQuest(game, playerId));
    }

    @PostMapping("/quest/drawParticipationCard")
    public GameStateDTO drawParticipationCard(@RequestParam String playerId) {
        return gameService.drawCardForQuestParticipation(game, playerId);
    }

    @PostMapping("/quest/attack")
    public ResponseEntity<AttackResult> submitAttack(@RequestBody AttackRequest request) {
        return ResponseEntity.ok(questService.processAttack(game, request));
    }

    @PostMapping("/quest/withdraw")
    public ResponseEntity<GameStateDTO> withdrawFromQuest(@RequestParam String playerId) {
        questService.getQuestParticipants().remove(playerId);
        return ResponseEntity.ok(gameService.createGameStateDTO(game));
    }


    @PostMapping("/quest/addShield")
    public GameStateDTO addShields(@RequestBody AddShieldRequest request) {
//        System.out.println("This is the number of players who won this quest:" + request.getPlayerIds());
        return gameService.addShieldsToWinners(game, request.getPlayerIds());
    }

    //handle the "ending of turn"
    @PostMapping("/endTurn")
    public GameStateDTO endCurrentPlayersTurn() {
        GameStateDTO newState = gameService.moveToNextPlayer(game);
//        System.out.println("We moved to the next player: " + newState.getCurrentPlayerId());
        return newState;
    }

    @PostMapping("/discardCards")
    @CrossOrigin(origins = "*")  // Make sure CORS is enabled
    public GameStateDTO discardCards(@RequestBody DiscardCardRequest request) {
        playerService.discardCardsFromHand(game, request);
        return gameService.createGameStateDTO(game);
    }

    @PostMapping("/quest/complete")
    public GameStateDTO completeQuest() {
        return gameService.handleQuestCompletion(game);
    }

}