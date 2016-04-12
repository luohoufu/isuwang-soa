package com.isuwang.soa.container.transaction;

import com.isuwang.soa.container.Container;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.transaction.api.GlobalTransactionFactory;

/**
 * TransactionContainer
 *
 * @author craneding
 * @date 16/4/11
 */
public class TransactionContainer implements Container {

    GlobalTransactionFactory transactionalManager = new GlobalTransactionFactory();

    @Override
    public void start() {
        if (SoaSystemEnvProperties.SOA_TRANSACTIONAL_ENABLE) {

        }
    }

    @Override
    public void stop() {

    }

}
