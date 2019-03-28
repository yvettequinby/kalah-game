package com.javafreelancedeveloper.kalah.controller;

import com.javafreelancedeveloper.kalah.dto.CreateGameRequestDTO;
import com.javafreelancedeveloper.kalah.dto.GameDTO;
import com.javafreelancedeveloper.kalah.dto.GameSummaryDTO;
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
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LandingPageController {

    private final GameService gameService;

    @GetMapping
    @RequestMapping({"", "/", "/index"})
    public String displayLanding(Model model) {
        log.debug("Displaying landing page");
        List<GameSummaryDTO> activeGames = gameService.listActiveGames();
        List<GameSummaryDTO> pendingGames = gameService.listPendingGames();
        model.addAttribute("createGameRequest", new CreateGameRequestDTO(null));
        model.addAttribute("activeGames", activeGames);
        model.addAttribute("pendingGames", pendingGames);
        return "landing";
    }


    @PostMapping
    @RequestMapping("/create")
    public String createGame(Model model, @Valid @ModelAttribute("createGameRequest") CreateGameRequestDTO input, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            GameDTO game = gameService.createGame(input);
            model.addAttribute("game", game);
            String url = "redirect:/game/%s/player/%s";
            return String.format(url, game.getId().toString(), game.getPlayerId().toString());
        } else {
            model.addAttribute("scrollToSelector", "#new-game-card");
            return "landing";
        }
    }


    @GetMapping
    @RequestMapping("/instructions")
    public String displayInstructions() {
        log.debug("Displaying instructions page");
        return "instructions";
    }


}
