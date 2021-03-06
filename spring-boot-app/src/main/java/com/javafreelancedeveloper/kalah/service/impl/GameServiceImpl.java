package com.javafreelancedeveloper.kalah.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javafreelancedeveloper.kalah.domain.Game;
import com.javafreelancedeveloper.kalah.domain.GameStatus;
import com.javafreelancedeveloper.kalah.dto.*;
import com.javafreelancedeveloper.kalah.exception.*;
import com.javafreelancedeveloper.kalah.repository.GameRepository;
import com.javafreelancedeveloper.kalah.service.GameService;
import com.javafreelancedeveloper.kalah.util.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private static final long MAX_GAME_TIME_MILLISECS = 1000L * 60L * 60L * 3L; // 3 hours
    private static final long MAX_GAME_TIME_WAIT = 1000L * 60L * 10L; // 10 minutes
    private static final long MAX_GAME_COUNT = 50L; // 50 games
    private static final Integer PLAYER_ONE_FIRST_PIT = 1;
    private static final Integer PLAYER_ONE_KALAH_NUMBER = 7;
    private static final Integer PLAYER_TWO_FIRST_PIT = 8;
    private static final Integer PLAYER_TWO_KALAH_NUMBER = 14;

    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;
    private final WebSocketUtil webSocketUtil;


    @Override
    public String convertGameToJson(GameDTO game) {
        return toJson(game);
    }


    @Scheduled(fixedRate = 10000)
    public void cleanUpExpiredGames() {
        long now = System.currentTimeMillis();
        gameRepository.findAllByStatus(GameStatus.GAME_OVER).forEach(game -> {
            gameRepository.delete(game);
            webSocketUtil.sendUpdateToWebSocket(new GameUpdateDTO(GameUpdateType.RAG, convertToSummary(game)));
        });
        gameRepository.findAll().forEach(game -> {
            GameUpdateType gameUpdateType = game.getStatus().equals(GameStatus.PENDING) ? GameUpdateType.RPG : GameUpdateType.RAG;
            if (now - game.getCreatedTimestamp().getTime() > MAX_GAME_TIME_MILLISECS) {
                log.info("Game " + game.getId() + " has timed-out. Deleting game.");
                gameRepository.delete(game);
                game.setStatus(GameStatus.TIME_OUT_INACTIVE);
                webSocketUtil.sendUpdateToWebSocket(convert(game, null, false, null));
                webSocketUtil.sendUpdateToWebSocket(new GameUpdateDTO(gameUpdateType, convertToSummary(game)));
            } else if (now - game.getUpdatedTimestamp().getTime() > MAX_GAME_TIME_WAIT) {
                log.info("Game " + game.getId() + " has timed-out. Deleting game.");
                gameRepository.delete(game);
                game.setStatus(GameStatus.TIME_OUT_DURATION);
                webSocketUtil.sendUpdateToWebSocket(convert(game, null, false, null));
                webSocketUtil.sendUpdateToWebSocket(new GameUpdateDTO(gameUpdateType, convertToSummary(game)));
            }
        });
    }


    @Override
    public List<GameSummaryDTO> listActiveGames() {
        List<GameSummaryDTO> activeGames = new ArrayList<>();
        gameRepository.findAll().forEach(game -> {
            if (GameStatus.PLAYER_ONE_TURN.equals(game.getStatus()) || GameStatus.PLAYER_TWO_TURN.equals(game.getStatus())) {
                activeGames.add(convertToSummary(game));
            }
        });
        return activeGames;
    }


    @Override
    public List<GameSummaryDTO> listPendingGames() {
        List<GameSummaryDTO> pendingGames = new ArrayList<>();
        gameRepository.findAllByStatus(GameStatus.PENDING).forEach(game -> pendingGames.add(convertToSummary(game)));
        return pendingGames;
    }


    @Override
    public GameDTO getGame(GameRequestDTO gameRequest) {
        Game game = gameRepository.findById(gameRequest.getGameId()).orElseThrow(GameNotFoundException::new);
        if (gameRequest.getPlayerId() == null) {
            return convert(game, null, false, null);
        } else {
            if (gameRequest.getPlayerId().equals(game.getPlayerOneId())) {
                return convert(game, gameRequest.getPlayerId(), true, null);
            } else if (gameRequest.getPlayerId().equals(game.getPlayerTwoId())) {
                return convert(game, gameRequest.getPlayerId(), false, null);
            } else {
                throw new GameInvalidAccessException();
            }
        }
    }


    @Override
    public GameDTO createGame(CreateGameRequestDTO createGameRequest) {
        long gameCount = gameRepository.count();
        if (gameCount >= MAX_GAME_COUNT) {
            throw new TooManyGamesException();
        } else {
            Game newGame = Game.builder()
                    .name(createGameRequest.getName())
                    .playerOneId(UUID.randomUUID())
                    .playerTwoId(UUID.randomUUID())
                    .status(GameStatus.PENDING)
                    .state(toJson(makeGame()))
                    .build();
            newGame = gameRepository.save(newGame);
            log.info("Player One created game " + newGame.getId());

            GameSummaryDTO gameSummary = convertToSummary(newGame);
            webSocketUtil.sendUpdateToWebSocket(new GameUpdateDTO(GameUpdateType.APG, gameSummary));

            return convert(newGame, newGame.getPlayerOneId(), true, null);
        }
    }

    @Override
    public GameDTO joinGame(UUID gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(GameNotFoundException::new);
        if (GameStatus.PENDING.equals(game.getStatus())) {
            game.setStatus(GameStatus.PLAYER_ONE_TURN);
            game = gameRepository.save(game);
            log.info("Player Two joined game " + game.getId());

            GameDTO gameDTO = convert(game, game.getPlayerTwoId(), false, null);
            GameSummaryDTO gameSummary = convertToSummary(game);
            webSocketUtil.sendUpdateToWebSocket(gameDTO);
            webSocketUtil.sendUpdateToWebSocket(new GameUpdateDTO(GameUpdateType.RPG, gameSummary));
            webSocketUtil.sendUpdateToWebSocket(new GameUpdateDTO(GameUpdateType.AAG, gameSummary));

            return gameDTO;
        } else {
            throw new GameNotPendingException();
        }
    }


    @Override
    public GameDTO makeMove(GameMoveDTO move) {
        Game game = gameRepository.findById(move.getGameId()).orElseThrow(GameNotFoundException::new);
        Map<Integer, Integer> gameState = fromJson(game.getState());
        validateMove(move, game, gameState);
        boolean isPlayerOne = isPlayerOne(move.getPlayerId(), game);
        Integer lastPitNumber = applyMove(move, game, gameState);
        boolean gameOver = handleGameOver(gameState);
        log.debug("Player " + move.getPlayerId() + " made move on " + move.getPitNumber() + " in game " + move.getGameId() + ".");
        if (gameOver) {
            game.setStatus(GameStatus.GAME_OVER);
            log.info("Game " + move.getGameId() + " is GAME OVER!");
        } else {
            boolean goAgain = getKalahNumber(isPlayerOne).equals(lastPitNumber);
            GameStatus nextStatus = isPlayerOne
                    ? (goAgain ? GameStatus.PLAYER_ONE_TURN : GameStatus.PLAYER_TWO_TURN)
                    : (goAgain ? GameStatus.PLAYER_TWO_TURN : GameStatus.PLAYER_ONE_TURN);
            game.setStatus(nextStatus);
        }
        game.setState(toJson(gameState));
        game = gameRepository.save(game);

        GameDTO gameDTO = convert(game, move.getPlayerId(), isPlayerOne, move.getPitNumber());
        webSocketUtil.sendUpdateToWebSocket(gameDTO);

        return gameDTO;
    }


    @Override
    public GameDTO quitGame(QuitGameRequestDTO quitGameRequest) {
        Game game = gameRepository.findById(quitGameRequest.getGameId()).orElseThrow(GameNotFoundException::new);
        boolean isPlayerOne = isPlayerOne(quitGameRequest.getPlayerId(), game);
        GameUpdateType gameUpdateType = game.getStatus().equals(GameStatus.PENDING) ? GameUpdateType.RPG : GameUpdateType.RAG;
        log.info("Player " + quitGameRequest.getPlayerId() + " has quit game " + quitGameRequest.getGameId() + ". GAME OVER!");
        game.setStatus(GameStatus.GAME_OVER_QUIT);
        GameDTO gameDTO = convert(game, quitGameRequest.getPlayerId(), isPlayerOne, null);
        GameSummaryDTO gameSummaryDTO = convertToSummary(game);
        game.setStatus(GameStatus.GAME_OVER);
        gameRepository.save(game);
        webSocketUtil.sendUpdateToWebSocket(gameDTO);
        webSocketUtil.sendUpdateToWebSocket(new GameUpdateDTO(gameUpdateType, gameSummaryDTO));
        return gameDTO;
    }


    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new HandledException(HandledException.MSG_UNEXPECTED, e);
        }
    }


    private Map<Integer, Integer> fromJson(String gameStateJson) {
        try {
            TypeReference<HashMap<Integer, Integer>> typeRef = new TypeReference<HashMap<Integer, Integer>>() {
            };
            return objectMapper.readValue(gameStateJson, typeRef);
        } catch (IOException e) {
            throw new HandledException(HandledException.MSG_UNEXPECTED, e);
        }
    }

    private boolean isPlayerOne(UUID playerId, Game game) {
        if (playerId.equals(game.getPlayerOneId()) || playerId.equals(game.getPlayerTwoId())) {
            return playerId.equals(game.getPlayerOneId());
        } else {
            throw new GameInvalidAccessException();
        }
    }


    public Integer applyMove(GameMoveDTO move, Game game, Map<Integer, Integer> gameState) {

        boolean isPlayerOne = isPlayerOne(move.getPlayerId(), game);

        Integer currentPitStoneCount = gameState.get(move.getPitNumber());

        gameState.put(move.getPitNumber(), 0); // pick up stones from selected pit

        // Sow the stones
        final AtomicInteger nextPitNumber = new AtomicInteger(move.getPitNumber() + 1);
        final AtomicInteger lastPitNumber = new AtomicInteger(move.getPitNumber());
        IntStream.range(0, currentPitStoneCount).forEach(
                (i) -> {
                    if (isSkipKalah(isPlayerOne, nextPitNumber.get())) {
                        nextPitNumber.getAndIncrement(); // Skip other player's Kalah
                    }
                    if (nextPitNumber.get() == 15) {
                        nextPitNumber.set(1); // keep us in the loop
                    }
                    addStone(gameState, nextPitNumber.get()); // put stone in the next pit
                    lastPitNumber.set(nextPitNumber.get());
                    nextPitNumber.getAndIncrement();
                }
        );

        // check if last stone landed in player's own empty pit
        boolean landedInOwnEmptyPit = isLandedOwnEmptyPit(isPlayerOne, lastPitNumber.get(), gameState);
        if (landedInOwnEmptyPit) { // if the sowing ended in the player's own pit
            Integer oppositePitNumber = 14 - lastPitNumber.get(); // find the opposite pit index
            Integer ownKalahNumber = getKalahNumber(isPlayerOne);
            moveStones(gameState, oppositePitNumber, ownKalahNumber);
            moveStones(gameState, lastPitNumber.get(), ownKalahNumber);
        }

        return lastPitNumber.get();
    }


    public void validateMove(GameMoveDTO move, Game game, Map<Integer, Integer> gameState) {
        boolean isPlayerOne = move.getPlayerId().equals(game.getPlayerOneId());
        boolean isPlayerTwo = move.getPlayerId().equals(game.getPlayerTwoId());
        if (!isPlayerOne && !isPlayerTwo) {
            throw new GameInvalidAccessException();
        }
        if (!GameStatus.PLAYER_ONE_TURN.equals(game.getStatus()) && !GameStatus.PLAYER_TWO_TURN.equals(game.getStatus())) {
            throw new GameInvalidMoveException(GameInvalidMoveException.MSG_NOT_CORRECT_STATE);
        }
        if (GameStatus.PLAYER_ONE_TURN.equals(game.getStatus()) && !isPlayerOne) {
            throw new GameInvalidMoveException(GameInvalidMoveException.MSG_NOT_YOUR_TURN);
        }
        if (GameStatus.PLAYER_TWO_TURN.equals(game.getStatus()) && !isPlayerTwo) {
            throw new GameInvalidMoveException(GameInvalidMoveException.MSG_NOT_YOUR_TURN);
        }
        if (isPlayerOne && (move.getPitNumber() < 1 || move.getPitNumber() > 6)) {
            throw new GameInvalidMoveException(GameInvalidMoveException.MSG_INVALID_PIT);
        }
        if (isPlayerTwo && (move.getPitNumber() < 8 || move.getPitNumber() > 13)) {
            throw new GameInvalidMoveException(GameInvalidMoveException.MSG_INVALID_PIT);
        }
        Integer currentPitStoneCount = gameState.get(move.getPitNumber());
        if (currentPitStoneCount <= 0) {
            throw new GameInvalidMoveException(GameInvalidMoveException.MSG_NO_STONES);
        }
    }


    private Map<Integer, Integer> makeGame() {
        Map<Integer, Integer> gameState = new LinkedHashMap<>();
        gameState.put(1, 4); // player one first pit
        gameState.put(2, 4);
        gameState.put(3, 4);
        gameState.put(4, 4);
        gameState.put(5, 4);
        gameState.put(6, 4);
        gameState.put(7, 0); // player one kalah
        gameState.put(8, 4); // player two first pit
        gameState.put(9, 4);
        gameState.put(10, 4);
        gameState.put(11, 4);
        gameState.put(12, 4);
        gameState.put(13, 4);
        gameState.put(14, 0); // player two kalah
        return gameState;
    }


    private boolean handleGameOver(Map<Integer, Integer> gameState) {
        if (!doesPlayerOneHaveStones(gameState)) {
            gameOverStoneShuffle(gameState, PLAYER_TWO_FIRST_PIT, PLAYER_TWO_KALAH_NUMBER);
            return true;
        } else if (!doesPlayerTwoHaveStones(gameState)) {
            gameOverStoneShuffle(gameState, PLAYER_ONE_FIRST_PIT, PLAYER_ONE_KALAH_NUMBER);
            return true;
        } else {
            return false;
        }
    }


    private void gameOverStoneShuffle(Map<Integer, Integer> gameState, int firstPitNumber, int kalahNumber) {
        IntStream.range(firstPitNumber, kalahNumber).forEach(
                (i) -> moveStones(gameState, i, kalahNumber)
        );
    }


    private boolean doesPlayerOneHaveStones(Map<Integer, Integer> gameState) {
        return doesPlayerHaveStones(gameState, PLAYER_ONE_FIRST_PIT, PLAYER_ONE_KALAH_NUMBER);
    }


    private boolean doesPlayerTwoHaveStones(Map<Integer, Integer> gameState) {
        return doesPlayerHaveStones(gameState, PLAYER_TWO_FIRST_PIT, PLAYER_TWO_KALAH_NUMBER);
    }


    private boolean doesPlayerHaveStones(Map<Integer, Integer> gameState, int min, int max) {
        return IntStream.range(min, max).anyMatch(i -> gameState.get(i) > 0);
    }


    private void moveStones(Map<Integer, Integer> playerPits, Integer fromPitNumber, Integer toPitNumber) {
        Integer newStoneCount = playerPits.get(fromPitNumber) + playerPits.get(toPitNumber);
        playerPits.put(fromPitNumber, 0);
        playerPits.put(toPitNumber, newStoneCount);
    }


    private boolean isLandedOwnEmptyPit(boolean isPlayerOne, Integer pitNumber, Map<Integer, Integer> gameState) {
        if (isPlayerOne) {
            return pitNumber >= 1 && pitNumber <= 6 && gameState.get(pitNumber) == 1;
        } else {
            return pitNumber >= 8 && pitNumber <= 13 && gameState.get(pitNumber) == 1;
        }
    }


    private void addStone(Map<Integer, Integer> playerPits, Integer pitNumber) {
        Integer newStoneCount = playerPits.get(pitNumber) + 1;
        playerPits.put(pitNumber, newStoneCount);
    }


    private boolean isSkipKalah(boolean isPlayerOne, Integer selectedPitNumber) {
        if (isPlayerOne) {
            return selectedPitNumber.equals(PLAYER_TWO_KALAH_NUMBER);
        } else {
            return selectedPitNumber.equals(PLAYER_ONE_KALAH_NUMBER);
        }
    }


    private Integer getKalahNumber(boolean isPlayerOne) {
        if (isPlayerOne) {
            return PLAYER_ONE_KALAH_NUMBER;
        } else {
            return PLAYER_TWO_KALAH_NUMBER;
        }
    }


    private GameSummaryDTO convertToSummary(Game game) {
        return new GameSummaryDTO(game.getId(),
                game.getName(),
                game.getStatus().getDescription(),
                game.getCreatedTimestamp().getTime(),
                game.getUpdatedTimestamp().getTime());
    }


    private GameDTO convert(Game game, UUID playerId, boolean isPlayerOne, Integer lastMovePit) {
        return new GameDTO(game.getId(),
                game.getName(),
                game.getStatus().name(),
                game.getStatus().getDescription(),
                game.getCreatedTimestamp().getTime(),
                game.getUpdatedTimestamp().getTime(),
                playerId,
                playerId == null ? "WATCHER" : (isPlayerOne ? "PLAYER ONE" : "PLAYER TWO"),
                fromJson(game.getState()),
                lastMovePit);
    }
}
