package com.zpf;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;

@SpringBootApplication(exclude = {MultipartAutoConfiguration.class})//排除这个类
@MapperScan(basePackages = "com.zpf.mapper.*")
public class Start {

    public static void main(String[] args) {

        SpringApplication.run(Start.class,args);
    }
}
