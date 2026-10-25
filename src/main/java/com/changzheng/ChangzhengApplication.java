package com.changzheng;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.changzheng.mapper")
public class ChangzhengApplication {
    public static void main(String[] args) { SpringApplication.run(ChangzhengApplication.class, args); }
}
