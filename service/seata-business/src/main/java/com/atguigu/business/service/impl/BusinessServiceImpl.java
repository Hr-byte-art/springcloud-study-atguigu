package com.atguigu.business.service.impl;

import com.atguigu.business.feign.StorageFeign;
import com.atguigu.business.service.BusinessService;
import com.atguigu.order.feign.AccountFeign;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * @author 王哈哈
 */
@Service
public class BusinessServiceImpl implements BusinessService {

    @Resource
    StorageFeign storageFeign;
    @Resource
    AccountFeign accountFeign;
    @Override
    public void purchase(String userId, String commodityCode, int orderCount) {
//        1. 扣减库存
        storageFeign.deduct(commodityCode, orderCount);
//        2. 创建订单
        accountFeign.debit(userId, orderCount);
    }
}
