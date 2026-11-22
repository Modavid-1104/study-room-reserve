package com.studyroom.studyroomreserve;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.studyroom.studyroomreserve.mapper")
@EnableScheduling   // 开启 Spring 定时任务
public class StudyRoomReserveApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyRoomReserveApplication.class, args);
    }
}
