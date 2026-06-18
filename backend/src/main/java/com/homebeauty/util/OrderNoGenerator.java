package com.homebeauty.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class OrderNoGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private static final int MAX_SEQUENCE = 9999;

    public static String generateOrderNo() {
        log.debug("开始生成订单号");
        
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        int sequence = SEQUENCE.incrementAndGet();
        if (sequence > MAX_SEQUENCE) {
            SEQUENCE.set(0);
            sequence = SEQUENCE.incrementAndGet();
        }
        
        Random random = new Random();
        int randomNum = random.nextInt(9000) + 1000;
        
        String orderNo = timestamp + String.format("%04d", sequence) + randomNum;
        
        log.debug("生成订单号: {}", orderNo);
        return orderNo;
    }
}
