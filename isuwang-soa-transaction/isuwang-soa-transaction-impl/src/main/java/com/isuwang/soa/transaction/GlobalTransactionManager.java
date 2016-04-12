package com.isuwang.soa.transaction;

import org.springframework.transaction.annotation.Transactional;

/**
 * Global Transaction Manager
 *
 * @author craneding
 * @date 16/4/12
 */
public class GlobalTransactionManager {

    public void init() {
        System.out.println("init");
    }

    public void destory() {
        System.out.println("destory");
    }

    @Transactional(value = "globalTransaction", rollbackFor = Exception.class)
    public void doJob() {

    }
}
