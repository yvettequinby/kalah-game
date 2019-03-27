package com.javafreelancedeveloper.kalah.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javafreelancedeveloper.kalah.domain.Game;
import com.javafreelancedeveloper.kalah.domain.GameStatus;
import com.javafreelancedeveloper.kalah.dto.GameMoveDTO;
import com.javafreelancedeveloper.kalah.exception.GameInvalidMoveException;
import com.javafreelancedeveloper.kalah.repository.GameRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.core.Is.is;

public class GameServiceImplTest {

    private static final UUID GAME_ID = UUID.randomUUID();
    private static final UUID P1_ID = UUID.randomUUID();
    private static final UUID P2_ID = UUID.randomUUID();
    private static final String GAME_NAME = "Test Game";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private GameRepository gameRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GameServiceImpl gameService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testValidateMove_incorrectStatus() {

        thrown.expect(GameInvalidMoveException.class);
        thrown.expectMessage(is(GameInvalidMoveException.MSG_NOT_CORRECT_STATE));

        Map<Integer, Integer> emptyGameState = new LinkedHashMap<>();
        Game game = makeTestGame(GameStatus.GAME_OVER, "");
        GameMoveDTO move = new GameMoveDTO(GAME_ID, P2_ID, 9);
        gameService.validateMove(move, game, emptyGameState);

    }


    @Test
    public void testValidateMove_notP1Turn() {

        thrown.expect(GameInvalidMoveException.class);
        thrown.expectMessage(is(GameInvalidMoveException.MSG_NOT_YOUR_TURN));

        Map<Integer, Integer> emptyGameState = new LinkedHashMap<>();
        Game game = makeTestGame(GameStatus.PLAYER_TWO_TURN, "");
        GameMoveDTO move = new GameMoveDTO(GAME_ID, P1_ID, 1);
        gameService.validateMove(move, game, emptyGameState);

    }


    @Test
    public void testValidateMove_notP2Turn() {

        thrown.expect(GameInvalidMoveException.class);
        thrown.expectMessage(is(GameInvalidMoveException.MSG_NOT_YOUR_TURN));

        Map<Integer, Integer> emptyGameState = new LinkedHashMap<>();
        final Game game = makeTestGame(GameStatus.PLAYER_ONE_TURN, "");
        final GameMoveDTO move = new GameMoveDTO(GAME_ID, P2_ID, 9);
        gameService.validateMove(move, game, emptyGameState);

    }


    @Test
    public void testValidateMove_invalidPitP1Low() {

        thrown.expect(GameInvalidMoveException.class);
        thrown.expectMessage(is(GameInvalidMoveException.MSG_INVALID_PIT));

        Map<Integer, Integer> emptyGameState = new LinkedHashMap<>();
        Game game = makeTestGame(GameStatus.PLAYER_ONE_TURN, "");
        GameMoveDTO move = new GameMoveDTO(GAME_ID, P1_ID, -1);
        gameService.validateMove(move, game, emptyGameState);

    }


    @Test
    public void testValidateMove_invalidPitP1High() {

        thrown.expect(GameInvalidMoveException.class);
        thrown.expectMessage(is(GameInvalidMoveException.MSG_INVALID_PIT));

        Map<Integer, Integer> emptyGameState = new LinkedHashMap<>();
        Game game = makeTestGame(GameStatus.PLAYER_ONE_TURN, "");
        GameMoveDTO move = new GameMoveDTO(GAME_ID, P1_ID, 7);
        gameService.validateMove(move, game, emptyGameState);

    }


    @Test
    public void testValidateMove_invalidPitP2Low() {

        thrown.expect(GameInvalidMoveException.class);
        thrown.expectMessage(is(GameInvalidMoveException.MSG_INVALID_PIT));

        Map<Integer, Integer> emptyGameState = new LinkedHashMap<>();
        Game game = makeTestGame(GameStatus.PLAYER_TWO_TURN, "");
        GameMoveDTO move = new GameMoveDTO(GAME_ID, P2_ID, 7);
        gameService.validateMove(move, game, emptyGameState);

    }


    @Test
    public void testValidateMove_invalidPitP2High() {

        thrown.expect(GameInvalidMoveException.class);
        thrown.expectMessage(is(GameInvalidMoveException.MSG_INVALID_PIT));

        Map<Integer, Integer> emptyGameState = new LinkedHashMap<>();
        Game game = makeTestGame(GameStatus.PLAYER_TWO_TURN, "");
        GameMoveDTO move = new GameMoveDTO(GAME_ID, P2_ID, 14);
        gameService.validateMove(move, game, emptyGameState);

    }

    @Test
    public void testValidateMove_emptyPit() {

        thrown.expect(GameInvalidMoveException.class);
        thrown.expectMessage(is(GameInvalidMoveException.MSG_NO_STONES));

        Map<Integer, Integer> gameState = new LinkedHashMap<>();
        gameState.put(1, 0);
        Game game = makeTestGame(GameStatus.PLAYER_ONE_TURN, "");
        GameMoveDTO move = new GameMoveDTO(GAME_ID, P1_ID, 1);
        gameService.validateMove(move, game, gameState);


    }


    private Game makeTestGame(GameStatus gameStatus, String gameStateJson) {
        return Game.builder()
                .id(GAME_ID)
                .name(GAME_NAME)
                .playerOneId(P1_ID)
                .playerTwoId(P2_ID)
                .status(gameStatus)
                .state(gameStateJson)
                .build();
    }
}
