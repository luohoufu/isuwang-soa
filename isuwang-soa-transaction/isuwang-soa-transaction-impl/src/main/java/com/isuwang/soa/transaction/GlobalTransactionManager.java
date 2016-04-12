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

    }

    public void destory() {

    }

    @Transactional(value = "kuaisuwang", rollbackFor = Exception.class)
    public void doJob() {

    }
}
