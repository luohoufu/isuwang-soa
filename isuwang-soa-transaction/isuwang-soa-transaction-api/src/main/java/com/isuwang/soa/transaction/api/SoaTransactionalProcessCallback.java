package com.isuwang.soa.transaction.api;

import com.isuwang.soa.core.SoaException;

/**
 * Soa Transactional ProcessCallback
 *
 * @author craneding
 * @date 16/4/11
 */
public interface SoaTransactionalProcessCallback<T> {

    T doInTransactionProcess() throws SoaException;

}
