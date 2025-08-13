package com.example.examine.service.EntityService;

import com.example.examine.dto.response.AlarmResponse;
import com.example.examine.dto.response.TableRespose.DataList;
import com.example.examine.dto.response.UserResponse.UserAlarmResponse;
import com.example.examine.entity.Alarm;
import com.example.examine.entity.Page;
import com.example.examine.entity.User.User;
import com.example.examine.entity.User.UserAlarm;
import com.example.examine.entity.User.UserAlarmId;
import com.example.examine.repository.AlarmRepository;
import com.example.examine.repository.UserRepository.UserAlarmRepository;
import com.example.examine.repository.UserRepository.UserPageRepository;
import com.example.examine.repository.UserRepository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AlarmService {
    private static final Logger log = LoggerFactory.getLogger(AlarmService.class);

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private static final Long TIMEOUT = 60L * 1000 * 60; // 60분
    private final UserAlarmRepository userAlarmRepo;
    private final AlarmRepository alarmRepo;
    private final UserRepository userRepo;
    private final UserPageRepository userPageRepo;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        return emitter;
    }

    public void sendToUser(Long userId, AlarmResponse alarm) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("alarm")
                        .data(alarm));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }

    public ResponseEntity<?> readAlarm(Long alarmId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        User user = (User) auth.getPrincipal();
        UserAlarmId id = new UserAlarmId(user.getId(), alarmId);

        UserAlarm item = userAlarmRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 알림은 존재하지 않습니다."));

        item.setRead(true);
        userAlarmRepo.save(item);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> readAllAlarm(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Long userId = ((User) auth.getPrincipal()).getId();
        userAlarmRepo.readAllByUserId(userId); // ✅ 단일 UPDATE

        return ResponseEntity.ok().build();
    }

    @Transactional(readOnly = true)
    public DataList<UserAlarmResponse> getAlarm(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Long userId = ((User) auth.getPrincipal()).getId();

        List<UserAlarm> alarms = userAlarmRepo.findAllByUserId(userId);

        return new DataList<>(alarms.stream()
                .map(UserAlarmResponse::fromEntity)
                .toList()
                );
    }


    public ResponseEntity<?> deleteAlarm(Long alarmId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Long userId = ((User) auth.getPrincipal()).getId();

        UserAlarmId id = new UserAlarmId(userId, alarmId);

        userAlarmRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<?> deleteAllAlarm(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Long userId = ((User) auth.getPrincipal()).getId();
        userAlarmRepo.deleteByUserId(userId);
        return ResponseEntity.ok().build();
    }

    @Transactional
    public void createPageAlarm(Page page, String message) {
        // 알림 저장
        Alarm alarm = Alarm.builder()
                .message(message)
                .page(page)
                .build();

        alarmRepo.save(alarm);

        // 북마크 유저 조회
        AlarmResponse dto = AlarmResponse.fromEntity(alarm);
        List<Long> userIds = userPageRepo.findUserIdsByPageId(page.getId());

        for (Long userId : userIds) {
            UserAlarm userAlarm = new UserAlarm();
            userAlarm.setId(new UserAlarmId(userId, alarm.getId()));
            userAlarm.setUser(userRepo.getReferenceById(userId));
            userAlarm.setAlarm(alarm);
            userAlarmRepo.save(userAlarm);

            log.info("알림 생성 page: {}, message: {}", page.getLink(), message);

            String json;
            try {
                json = objectMapper.writeValueAsString(dto);
            } catch (Exception e) {
                throw new RuntimeException("알림 직렬화 실패", e);
            }

            // 채널로 발행
            stringRedisTemplate.convertAndSend("alarm:user:" + userId, json);
        }
    }

    @Transactional
    public void createNoticeAlarm(String message) {
        // 알림 저장
        Alarm alarm = Alarm.builder()
                .message(message)
                .build();

        alarmRepo.save(alarm);

        AlarmResponse dto = AlarmResponse.fromEntity(alarm);
        List<User> users = userRepo.findAll();

        for (User user : users) {
            UserAlarm userAlarm = UserAlarm.builder()
                    .id(new UserAlarmId(user.getId(), alarm.getId()))
                    .user(user)
                    .alarm(alarm)
                    .build();

            userAlarmRepo.save(userAlarm);

            log.info("공지 생성 message: {}", message);
            String json;

            try {
                json = objectMapper.writeValueAsString(dto);
            } catch (Exception e) {
                throw new RuntimeException("알림 직렬화 실패", e);
            }
            // 채널로 발행
            stringRedisTemplate.convertAndSend("alarm:user:" + user.getId(), json);
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteOldAlarms() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        alarmRepo.deleteByCreatedAtBefore(cutoff);
        log.info("🔔 7일 지난 알림 삭제 완료 - 기준 시각: {}", cutoff);
    }

}
