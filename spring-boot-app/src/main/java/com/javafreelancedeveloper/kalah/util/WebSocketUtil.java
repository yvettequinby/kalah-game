package com.javafreelancedeveloper.kalah.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javafreelancedeveloper.kalah.dto.GameDTO;
import com.javafreelancedeveloper.kalah.dto.GameUpdateDTO;
import com.javafreelancedeveloper.kalah.exception.HandledException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketUtil {

    private final SimpMessagingTemplate template;
    private final ObjectMapper objectMapper;

    public void sendUpdateToWebSocket(GameDTO game) {
            try {
                String gameJson = objectMapper.writeValueAsString(game);
                template.convertAndSend("/topic/game/" + game.getId(), gameJson);
            } catch (JsonProcessingException e) {
                log.error("Error transforming game to JSON", e);
                throw new HandledException(HandledException.MSG_UNEXPECTED, e);
            }
    }

    public void sendUpdateToWebSocket(GameUpdateDTO gameUpdate) {
        try {
            String gameJson = objectMapper.writeValueAsString(gameUpdate);
            template.convertAndSend("/topic/landing", gameJson);
        } catch (JsonProcessingException e) {
            log.error("Error transforming gameUpdate to JSON", e);
            throw new HandledException(HandledException.MSG_UNEXPECTED, e);
        }
    }
}
