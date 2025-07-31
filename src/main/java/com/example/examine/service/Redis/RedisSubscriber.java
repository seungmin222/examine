package com.example.examine.service.Redis;


import com.example.examine.service.EntityService.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final AlarmService alarmService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String alarmId = new String(message.getBody());

        if (channel.startsWith("alarm:user:")) {
            Long userId = Long.parseLong(channel.replace("alarm:user:", ""));
            alarmService.sendToUser(userId, alarmId);
        }
    }
}
