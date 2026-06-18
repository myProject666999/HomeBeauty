package com.homebeauty.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class BookingSlotService {

    private static final String BOOKING_SLOT_KEY = "homebeauty:booking:slot:";
    private static final String BOOKING_LOCK_KEY = "homebeauty:booking:lock:";
    private static final String CHECK_IN_KEY = "homebeauty:checkin:";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RESERVE_SCRIPT =
            "local key = KEYS[1]\n" +
            "local slot = ARGV[1]\n" +
            "local orderId = ARGV[2]\n" +
            "local userId = ARGV[3]\n" +
            "local exists = redis.call('hexists', key, slot)\n" +
            "if exists == 1 then\n" +
            "    return 0\n" +
            "end\n" +
            "redis.call('hset', key, slot, cjson.encode({orderId=orderId, userId=userId, status='RESERVED'}))\n" +
            "redis.call('expire', key, 86400)\n" +
            "return 1";

    public boolean reserveSlot(Long artisanId, LocalDate date, String timeSlot, Long orderId, Long userId) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = BOOKING_SLOT_KEY + artisanId + ":" + dateStr;
        String lockKey = BOOKING_LOCK_KEY + artisanId + ":" + dateStr + ":" + timeSlot;

        log.debug("尝试预订时段: artisanId={}, date={}, timeSlot={}, orderId={}", artisanId, dateStr, timeSlot, orderId);

        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, orderId.toString(), 5, TimeUnit.MINUTES);
        if (locked == null || !locked) {
            log.warn("获取时段锁获取失败，可能正在处理中: {}", timeSlot);
            return false;
        }

        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(RESERVE_SCRIPT);
            script.setResultType(Long.class);

            Long result = redisTemplate.execute(script,
                    Collections.singletonList(key),
                    timeSlot, orderId.toString(), userId.toString());

            boolean success = result != null && result == 1;
            if (success) {
                log.info("时段预订成功: artisanId={}, date={}, timeSlot={}, orderId={}", 
                        artisanId, dateStr, timeSlot, orderId);
            } else {
                log.warn("时段已被占用: artisanId={}, date={}, timeSlot={}", artisanId, dateStr, timeSlot);
            }
            return success;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public void releaseSlot(Long artisanId, LocalDate date, String timeSlot) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = BOOKING_SLOT_KEY + artisanId + ":" + dateStr;
        
        log.debug("释放时段: artisanId={}, date={}, timeSlot={}", artisanId, dateStr, timeSlot);
        
        redisTemplate.opsForHash().delete(key, timeSlot);
        log.info("时段已释放: artisanId={}, date={}, timeSlot={}", artisanId, dateStr, timeSlot);
    }

    public boolean isSlotAvailable(Long artisanId, LocalDate date, String timeSlot) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = BOOKING_SLOT_KEY + artisanId + ":" + dateStr;
        
        Boolean hasKey = redisTemplate.opsForHash().hasKey(key, timeSlot);
        boolean available = hasKey == null || !hasKey;
        
        log.debug("检查时段可用性: artisanId={}, date={}, timeSlot={}, available={}", 
                artisanId, dateStr, timeSlot, available);
        
        return available;
    }

    public List<Object> getOccupiedSlots(Long artisanId, LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = BOOKING_SLOT_KEY + artisanId + ":" + dateStr;
        
        Set<Object> slots = redisTemplate.opsForHash().keys(key);
        log.debug("获取手艺人{}在{}的已占用时段: {}", artisanId, dateStr, slots);
        
        return slots != null ? slots.stream().toList() : Collections.emptyList();
    }

    public String generateCheckInCode(Long orderId) {
        String code = String.format("%06d", (int) ((Math.random() * 900000) + 100000));
        String key = CHECK_IN_KEY + orderId;
        
        redisTemplate.opsForValue().set(key, code, 24, TimeUnit.HOURS);
        log.debug("生成核销码: orderId={}, code={}", orderId, code);
        
        return code;
    }

    public boolean verifyCheckInCode(Long orderId, String code) {
        String key = CHECK_IN_KEY + orderId;
        String storedCode = (String) redisTemplate.opsForValue().get(key);
        
        boolean valid = code != null && code.equals(storedCode);
        log.debug("验证核销码: orderId={}, inputCode={}, valid={}", orderId, code, valid);
        
        return valid;
    }

    public void removeCheckInCode(Long orderId) {
        String key = CHECK_IN_KEY + orderId;
        redisTemplate.delete(key);
        log.debug("删除核销码: orderId={}", orderId);
    }

    public void markCheckIn(Long orderId, Long artisanId) {
        String key = CHECK_IN_KEY + "status:" + orderId;
        redisTemplate.opsForHash().put(key, "status", "CHECKED_IN");
        redisTemplate.opsForHash().put(key, "artisanId", artisanId.toString());
        redisTemplate.opsForHash().put(key, "checkInTime", 
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        
        log.info("标记到场核销成功: orderId={}, artisanId={}", orderId, artisanId);
    }

    public Map<Object, Object> getCheckInStatus(Long orderId) {
        String key = CHECK_IN_KEY + "status:" + orderId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        log.debug("获取核销状态: orderId={}, status={}", orderId, entries);
        return entries;
    }
}
