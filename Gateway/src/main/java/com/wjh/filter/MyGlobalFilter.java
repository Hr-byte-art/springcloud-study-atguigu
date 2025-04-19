package com.wjh.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author 王哈哈
 */
@Component
@Slf4j
public class MyGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("请求路径：{}", request.getURI());
        ServerHttpResponse response = exchange.getResponse();
        log.info("响应状态码：{}", response.getStatusCode());
        long start = System.currentTimeMillis();
        log.info("请求开始时间：{}", start);
        // =============================上面是前置逻辑==============================================
        return chain.filter(exchange)
                .doFinally(
                        result -> {
                            // =============================下面是后置逻辑==============================================
                            long end = System.currentTimeMillis();

                            log.info("耗时：{} ms", end - start);
                        }
                );
    }

    /**
     * 优先级
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
