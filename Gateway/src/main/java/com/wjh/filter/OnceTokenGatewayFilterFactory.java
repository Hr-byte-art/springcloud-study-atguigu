package com.wjh.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * @author 王哈哈
 */
@Component
@Slf4j
public class OnceTokenGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {
    @Override
    public GatewayFilter apply(NameValueConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
                return chain.filter(exchange).then(
                        Mono.fromRunnable(() -> {
                            ServerHttpResponse response = exchange.getResponse();
                            HttpHeaders headers = response.getHeaders();
                            String value = config.getValue();
                            if ("uuid".equalsIgnoreCase(value)){
                                value = UUID.randomUUID().toString();
                                log.info("生成uuid：{}", value);
                            }
                            if ("jwt".equalsIgnoreCase(value)){
                                value = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNzEzMjg5ODQwLCJleHAiOjE3MTMyOTM0NDB9.6Yt7kZ4n7XqY2wTJ1f7d9y7zv8Vn7Wk9l7Yr6m6s3Z8";
                            }
                            headers.add(config.getName(), value);
                        })
                );
            }
        };
    }
}
