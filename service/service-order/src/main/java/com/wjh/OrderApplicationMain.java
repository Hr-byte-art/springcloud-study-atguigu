package com.wjh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author 王哈哈
 * @Date 2025/4/11 21:11:35
 * @Description
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.wjh.feign"})
public class OrderApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplicationMain.class, args);
    }
}
