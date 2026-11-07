package com.weishixiang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.weishixiang.mapper")
public class WeishixiangApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeishixiangApplication.class, args);
    }
}
