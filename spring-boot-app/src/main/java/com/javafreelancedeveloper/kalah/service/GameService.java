package com.javafreelancedeveloper.kalah.service;

import com.javafreelancedeveloper.kalah.dto.*;

import java.util.List;
import java.util.UUID;

public interface GameService {

    List<GameSummaryDTO> listActiveGames();

    List<GameSummaryDTO> listPendingGames();

    GameDTO getGame(GameRequestDTO gameRequest);

    GameDTO createGame(CreateGameRequestDTO createGameRequest);

    GameDTO joinGame(UUID gameId);

    GameDTO makeMove(GameMoveDTO move);

    GameDTO quitGame(QuitGameRequestDTO quitGameRequest);
}
