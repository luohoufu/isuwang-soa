package com.isuwang.soa.container.transaction;

import com.isuwang.soa.container.Container;
import com.isuwang.soa.core.SoaSystemEnvProperties;

/**
 * TransactionContainer
 *
 * @author craneding
 * @date 16/4/11
 */
public class TransactionContainer implements Container {

    @Override
    public void start() {
        if (SoaSystemEnvProperties.SOA_TRANSACTIONAL_ENABLE) {
            //GlobalTransactionFactory.setGlobalTransactionService();
            //GlobalTransactionFactory.setGlobalTransactionProcessService();
        }
    }

    @Override
    public void stop() {

    }

}
