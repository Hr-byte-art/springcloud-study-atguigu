package com.wjh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @Author 王哈哈
 * @Date 2025/4/13 00:25:12
 * @Description
 */
@Component
@ConfigurationProperties(prefix = "order")
@Data
public class ApplicationConfig {
    private String timeout;
    // auto-confirm ---> autoConfirm
    private String autoConfirm;
    private String dbUrl;
}
