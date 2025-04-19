package com.atguigu.account.service;

/**
 * @author 王哈哈
 */
public interface AccountService {

    /**
     * 从用户账户中扣减
     * @param userId  用户id
     * @param money   扣减金额
     */
    void debit(String userId, int money);
}
