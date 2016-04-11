package com.isuwang.soa.transaction.api;

import com.isuwang.soa.core.SoaException;

/**
 * Soa Transactional ProcessCallback WithoutResult
 *
 * @author craneding
 * @date 16/4/11
 */
public abstract class SoaTransactionalProcessCallbackWithoutResult implements SoaTransactionalProcessCallback<Object> {

    public Object doInTransactionProcess() throws SoaException {
        doInTransactionProcessWithoutResult();

        return null;
    }

    protected abstract void doInTransactionProcessWithoutResult() throws SoaException;

}
