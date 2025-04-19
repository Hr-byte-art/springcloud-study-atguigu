package com.atguigu.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author 王哈哈
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu.business.feign")
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class SeataBusinessMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeataBusinessMainApplication.class, args);
    }
}
