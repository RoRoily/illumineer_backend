package com.buaa01.illumineer_backend;

import com.buaa01.illumineer_backend.im.IMServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.buaa01.illumineer_backend.service.client")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        new Thread(() -> {
            try {
                new IMServer().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
