package com.wjh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Author 王哈哈
 * @Date 2025/4/11 21:31:30
 * @Description 启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProductApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplicationMain.class, args);
    }
}
