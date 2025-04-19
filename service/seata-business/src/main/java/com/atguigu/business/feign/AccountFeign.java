package com.atguigu.business.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 王哈哈
 */
@FeignClient(value ="seata-account")
public interface AccountFeign {
    @GetMapping("/debit")
    String debit(@RequestParam("userId") String userId,
                        @RequestParam("money") int money);
}
