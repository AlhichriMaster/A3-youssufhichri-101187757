package org.example.controller;

import org.example.dto.response.GameStateDTO;
import org.example.model.Game;
import org.example.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private Game game;

    @Autowired
    private GameService gameService;

    @GetMapping("/state")
    public GameStateDTO getGameState() {
        return gameService.createGameStateDTO(game);
    }

    @PostMapping("/start")
    public GameStateDTO startGame() {
        return gameService.startGame(game);
    }

    @PostMapping("/playTurn")
    public GameStateDTO playTurn() {
        return gameService.playTurn(game);
    }
}