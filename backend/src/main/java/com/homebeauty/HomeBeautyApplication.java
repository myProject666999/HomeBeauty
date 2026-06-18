package com.homebeauty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.homebeauty.mapper")
@EnableScheduling
public class HomeBeautyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeBeautyApplication.class, args);
    }
}
