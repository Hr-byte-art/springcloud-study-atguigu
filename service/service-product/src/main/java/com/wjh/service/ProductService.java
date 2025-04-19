package com.wjh.service;

import com.wjh.entity.Product;

import java.net.URI;
import java.util.List;

/**
 * @Author 王哈哈
 * @Date 2025/4/12 22:03:35
 * @Description
 */

public interface ProductService {
    /**
     * @param id
     * @return
     */
    Product getProductById(Integer id) throws InterruptedException;

    List<URI> getInstances();
}
