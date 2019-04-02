package com.javafreelancedeveloper.kalah.controller;

import com.javafreelancedeveloper.kalah.dto.GameDTO;
import com.javafreelancedeveloper.kalah.dto.GameRequestDTO;
import com.javafreelancedeveloper.kalah.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
public class DisplayGameController {

    private final GameService gameService;

    @GetMapping
    @RequestMapping("/{gameId}/player/{playerId}")
    public String displayGame(@PathVariable UUID gameId, @PathVariable UUID playerId, Model model) {
        GameDTO game = gameService.getGame(new GameRequestDTO(gameId, playerId));
        String gameJson = gameService.convertGameToJson(game);
        model.addAttribute("game", game);
        model.addAttribute("gameJson", gameJson);
        return "game";
    }

    @GetMapping
    @RequestMapping("/{gameId}/watch")
    public String watchGame(@PathVariable UUID gameId, Model model) {
        GameDTO game = gameService.getGame(new GameRequestDTO(gameId, null));
        String gameJson = gameService.convertGameToJson(game);
        model.addAttribute("game", game);
        model.addAttribute("gameJson", gameJson);
        return "game";
    }

    @GetMapping
    @RequestMapping("/{gameId}/join")
    public String joinGame(@PathVariable UUID gameId) {
        GameDTO game = gameService.joinGame(gameId);
        String url = "redirect:/game/%s/player/%s";
        return String.format(url, game.getId().toString(), game.getPlayerId().toString());
    }

}
