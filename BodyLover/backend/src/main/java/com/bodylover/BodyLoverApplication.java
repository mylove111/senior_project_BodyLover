package com.bodylover;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.bodylover.mapper")
public class BodyLoverApplication {

    public static void main(String[] args) {
        SpringApplication.run(BodyLoverApplication.class, args);
    }

}
