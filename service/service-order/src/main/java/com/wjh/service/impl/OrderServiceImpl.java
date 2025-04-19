package com.wjh.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.wjh.entity.Order;
import com.wjh.entity.Product;
import com.wjh.feign.ProductFeignClient;
import com.wjh.service.OrderService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @Author 王哈哈
 * @Date 2025/4/12 22:12:07
 * @Description
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    DiscoveryClient discoveryClient;

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Resource
    ProductFeignClient productFeignClient;

    @Resource
    RestTemplate restTemplate;

    @SentinelResource(value = "createOrder", blockHandler = "getProductByIdBlockHandler")
    @Override
    public Order createOrder(Integer productId , HttpServletRequest request) {
        Product product = getProductByIdWithLoadBalance(productId);
        log.info("createOrder方法被调用");
        return getOrder(productId, product);
    }

    @Override
    public Order createOrderByFeign(Integer productId) {
        Product product = productFeignClient.getProductById(productId);
        return getOrder(productId, product);

    }

    @Override
    public List<URI> getProductURI() {
        return discoveryClient.getInstances("service-product").stream().map(
                ServiceInstance::getUri
        ).collect(Collectors.toList());
    }

    private Order getOrder(Integer productId, Product product) {
        Order order = new Order();
        order.setId(new Random().nextInt());
        order.setProductName(product.getName());
        order.setProductId(productId);
        order.setProductNum(product.getCount());
        BigDecimal totalAccount = product.getPrice().multiply(new BigDecimal(product.getCount()));
        order.setTotalAccount(totalAccount);
        order.setProducts(Arrays.asList(product));
        return order;
    }


    /**
     * 远程调用根据id查询商品信息
     * @param id
     * @return
     */
    public Product getProductById(Integer id) {
        ServiceInstance instance = discoveryClient.getInstances("service-product").get(0);
        URI uri = instance.getUri();
//        try {
//            SphU.entry("getProductById");
//        } catch (BlockException e) {
//            throw new RuntimeException(e);
//        }
        log.info("调用商品服务，地址：{}" , uri);
        return restTemplate.getForEntity(uri + "/getProductById/"+id, Product.class).getBody();
    }

    public Product getProductByIdWithLoadBalance(Integer id) {
//        ServiceInstance instance = discoveryClient.getInstances("service-product").get(0);
//        URI uri = instance.getUri();
        ServiceInstance choose = loadBalancerClient.choose("service-product");
        URI uri = choose.getUri();
        log.info("调用商品服务，地址：{}" , uri);
        return restTemplate.getForEntity(uri + "/getProductById/"+id, Product.class).getBody();
    }

    public Product getProductByIdWithLoadBalanceByAnnotation(Integer id) {
//        ServiceInstance choose = loadBalancerClient.choose("service-product");
//        URI uri = choose.getUri();
//        log.info("调用商品服务，地址：{}" , uri);
        return restTemplate.getForEntity("http://service-product/getProductById/" + id, Product.class).getBody();
    }

    /**
     * 熔断处理
     * @param productId
     * @return
     */
    public Order getProductByIdBlockHandler(Integer productId , BlockException e) {
        Order order = new Order();
        order.setId(new Random().nextInt());
        order.setProductName("未知商品"+e.getClass());
        order.setProductId(productId);
        order.setProductNum(99999999);
        order.setTotalAccount(new BigDecimal("999999"));
        order.setProducts(Arrays.asList(new Product()));
        return order;
    }
}
