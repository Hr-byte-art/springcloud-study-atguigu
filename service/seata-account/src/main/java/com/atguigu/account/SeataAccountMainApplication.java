package com.atguigu.account;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * @author 王哈哈
 */
@MapperScan("com.atguigu.account.mapper")
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class SeataAccountMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeataAccountMainApplication.class, args);
    }
}
