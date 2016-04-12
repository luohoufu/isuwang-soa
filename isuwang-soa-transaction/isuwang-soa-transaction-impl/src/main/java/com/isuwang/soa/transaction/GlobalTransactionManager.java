package com.isuwang.soa.transaction;

import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Global Transaction Manager
 *
 * @author craneding
 * @date 16/4/12
 */
public class GlobalTransactionManager {

    private AtomicBoolean working = new AtomicBoolean(false);

    @Transactional(value = "globalTransaction", rollbackFor = Exception.class)
    public void doJob() {
        if(working.get())
            return;

        working.set(true);

        try {
            System.out.println("--- done ---");
        } finally {
            working.set(false);
        }
    }
}
