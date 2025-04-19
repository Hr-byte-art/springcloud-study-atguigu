package com.wjh.feign.fallback;

import com.wjh.entity.Product;
import com.wjh.feign.ProductFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

/**
 * @Author 王哈哈
 * @Date 2025/4/13 21:22:18
 * @Description
 */
@Component
public class ProductFeignFallback implements ProductFeignClient {
    private static final Logger log = LoggerFactory.getLogger(ProductFeignFallback.class);

    @Override
    public Product getProductById(Integer id) {
        Product product = new Product();
        product.setId(new Random().nextInt());
        product.setName("未知商品");
        product.setPrice(new BigDecimal("999999"));
        product.setCount(99999999);
        product.setVersion(1);
        log.info("兜底回调");
        return product;
    }
}
