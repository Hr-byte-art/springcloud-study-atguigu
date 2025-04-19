package com.wjh.service.impl;

import com.wjh.entity.Product;
import com.wjh.service.ProductService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

/**
 * @Author 王哈哈
 * @Date 2025/4/12 22:04:28
 * @Description
 */
@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    @Resource
    DiscoveryClient discoveryClient;

    @Override
    public Product getProductById(Integer id) throws InterruptedException {
        Product product = new Product();
        product.setId(id);
        product.setName("苹果");
        product.setPrice(new BigDecimal("10"));
        product.setCount(100);
        product.setVersion(1);
//        Thread.sleep(2000);
        return product;
    }

    @Override
    public List<URI> getInstances() {
        List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        List<URI> uriList = instances.stream().map(
                ServiceInstance::getUri
        ).toList();
        return uriList;
    }
}
