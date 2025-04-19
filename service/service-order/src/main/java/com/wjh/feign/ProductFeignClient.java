package com.wjh.feign;

import com.wjh.entity.Product;
import com.wjh.feign.fallback.ProductFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author 王哈哈
 * @Date 2025/4/13 16:49:31
 * @Description
 */
@FeignClient(value = "service-product" , fallback = ProductFeignFallback.class)
public interface ProductFeignClient {

    @GetMapping("/api/product/getProductById/{id}")
    Product getProductById(@PathVariable("id") Integer id);
}
