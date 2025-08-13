package com.example.examine.service.Redis;


import com.example.examine.dto.response.AlarmResponse;
import com.example.examine.service.EntityService.AlarmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(RedisSubscriber.class);
    private final AlarmService alarmService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), java.nio.charset.StandardCharsets.UTF_8);
        String json    = new String(message.getBody(),    java.nio.charset.StandardCharsets.UTF_8);

        try {
            AlarmResponse dto = objectMapper.readValue(json, AlarmResponse.class);
            Long userId = Long.parseLong(channel.substring("alarm:user:".length()));
            alarmService.sendToUser(userId, dto); // SseEmitter.event().data(dto) 등
        } catch (Exception e) {
           log.error(e.getMessage());
        }
    }


}
