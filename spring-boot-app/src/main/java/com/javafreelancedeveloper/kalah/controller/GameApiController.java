package com.javafreelancedeveloper.kalah.controller;


import com.javafreelancedeveloper.kalah.dto.GameMoveDTO;
import com.javafreelancedeveloper.kalah.dto.QuitGameRequestDTO;
import com.javafreelancedeveloper.kalah.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameApiController {

    private final GameService gameService;

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void makeMove(@RequestBody GameMoveDTO gameMove) {
        gameService.makeMove(gameMove);
    }

    @PutMapping
    @RequestMapping("/{gameId}/quit/player/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    public void quitGame(@PathVariable UUID gameId, @PathVariable UUID playerId) {
        gameService.quitGame(new QuitGameRequestDTO(gameId, playerId));
    }


}
