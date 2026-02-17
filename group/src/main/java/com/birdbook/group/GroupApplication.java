package com.birdbook.group;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GroupApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupApplication.class, args);
    }

}
