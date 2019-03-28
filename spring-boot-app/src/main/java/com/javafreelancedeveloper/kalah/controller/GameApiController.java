package com.javafreelancedeveloper.kalah.controller;


import com.javafreelancedeveloper.kalah.dto.GameDTO;
import com.javafreelancedeveloper.kalah.dto.GameMoveDTO;
import com.javafreelancedeveloper.kalah.service.GameService;
import com.javafreelancedeveloper.kalah.util.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameApiController {

    private final GameService gameService;
    private final WebSocketUtil webSocketUtil;

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void makeMove(@RequestBody GameMoveDTO gameMove) {
        GameDTO game = gameService.makeMove(gameMove);
        webSocketUtil.sendUpdateToWebSocket(game);

    }


}
