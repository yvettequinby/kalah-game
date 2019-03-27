package com.javafreelancedeveloper.kalah.controller;

import com.javafreelancedeveloper.kalah.dto.CreateGameRequestDTO;
import com.javafreelancedeveloper.kalah.dto.GameSummaryDTO;
import com.javafreelancedeveloper.kalah.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        model.addAttribute("activeGames", activeGames);
        model.addAttribute("pendingGames", pendingGames);
        return "landing";
    }


}
