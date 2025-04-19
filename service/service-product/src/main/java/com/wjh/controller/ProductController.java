package com.wjh.controller;

import com.wjh.entity.Product;
import com.wjh.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * @Author 王哈哈
 * @Date 2025/4/12 22:01:46
 * @Description
 */
@RestController
@Slf4j
//@RequestMapping("/product")
public class ProductController {
    @Autowired
    ProductService service;
    @GetMapping("/getProductById/{id}" )
    public Product getProductById(@PathVariable("id") Integer id,
                                  HttpServletRequest request) throws InterruptedException {
        log.info("拦截器自定义的X-Token:{}",request.getHeader("X-Token"));
//        int a = 1/0;
       return service.getProductById(id);
    }

    @GetMapping("getInstances")
    public List<URI> getInstances(){
        return service.getInstances();
    }
}
