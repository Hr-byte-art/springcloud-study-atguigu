package com.wjh.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.wjh.config.ApplicationConfig;
import com.wjh.entity.Order;
import com.wjh.entity.Product;
import com.wjh.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @Author 王哈哈
 * @Date 2025/4/12 22:06:50
 * @Description
 */
@RestController
//@RefreshScope
@Slf4j
//@RequestMapping("/order")
public class OrderController {

//    @Value("${order.timeout}")
//    private String timeout;
//    @Value("${order.auto-confirm}")
//    private String autoConfirm;

    @Autowired
    OrderService orderService;
    @Autowired
    ApplicationConfig applicationConfig;

    @GetMapping(value = "/createOrder/{productId}")
    public Order createOrder(@PathVariable("productId") Integer productId, HttpServletRequest request) {
        return orderService.createOrder(productId,request);
    }

    @GetMapping(value = "/createKillOrder/{productId}")
    @SentinelResource(value = "KillOrder" , fallback = "createKillOrderBlockHandler")
    public Order createKillOrder(@PathVariable("productId") Integer productId,
                                 HttpServletRequest request) {
        return orderService.createOrder(productId , request);
    }

    /**
     * 兜底回调方法
     * @param productId
     * @param e
     * @return
     */
    public Order createKillOrderBlockHandler(Integer productId , BlockException e) {
        log.info("createKillOrderBlockHandler");
        Order order = new Order();
        order.setId(new Random().nextInt());
        order.setProductName("未知商品"+e.getClass());
        order.setProductId(productId);
        order.setProductNum(99999999);
        order.setTotalAccount(new BigDecimal("999999"));
        order.setProducts(Arrays.asList(new Product()));
        log.info("createKillOrderBlockHandler 兜底回调");
        return order;
    }

    @GetMapping(value = "/getOrderConfig")
    public String getOrderConfig() {
        return "timeout:" + applicationConfig.getTimeout() +
                ",autoConfirm:" + applicationConfig.getAutoConfirm()+
                ",dbUrl:"+applicationConfig.getDbUrl();
    }

    @GetMapping(value = "/createOrderByFeign/{productId}")
    public Order createOrderByFeign(@PathVariable("productId") Integer productId) {
        return orderService.createOrderByFeign(productId);
    }

    @GetMapping(value = "getUri")
    public List<URI> getProductURI(){
        return orderService.getProductURI();
    }

    @GetMapping(value = "writeDb")
    public String writeDb(){
    log.info("writeDb");
    return "writeDb";
    }

    @GetMapping(value = "readDb")
    public String readDb(){
        log.info("readDb");
        return "readDb";
    }

}
