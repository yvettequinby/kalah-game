package com.javafreelancedeveloper.kalah.controller;

import com.javafreelancedeveloper.kalah.dto.CreateGameRequestDTO;
import com.javafreelancedeveloper.kalah.dto.GameDTO;
import com.javafreelancedeveloper.kalah.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/create")
@RequiredArgsConstructor
public class CreateGameController {

    private final GameService gameService;

    @GetMapping
    public String displayCreateGame(Model model) {
        model.addAttribute("createGameRequest", new CreateGameRequestDTO(null));
        return "create-game";
    }

    @PostMapping
    public String createGame(Model model, @Valid @ModelAttribute("createGameRequest") CreateGameRequestDTO input, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            GameDTO game = gameService.createGame(input);
            model.addAttribute("game", game);
            String url = "redirect:/game/%s/player/%s";
            return String.format(url, game.getId().toString(), game.getPlayerId().toString());
        } else {
            return "create-game";
        }
    }
}
