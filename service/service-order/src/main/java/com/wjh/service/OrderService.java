package com.wjh.service;

import com.wjh.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.List;

/**
 * @Author 王哈哈
 * @Date 2025/4/12 22:07:53
 * @Description
 */
public interface OrderService {

    Order createOrder(Integer productId , HttpServletRequest request);

    Order createOrderByFeign(Integer productId);

    List<URI> getProductURI();
}
