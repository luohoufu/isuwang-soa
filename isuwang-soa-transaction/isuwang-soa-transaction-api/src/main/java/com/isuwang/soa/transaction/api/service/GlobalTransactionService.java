
package com.isuwang.soa.transaction.api.service;

import com.isuwang.soa.core.Processor;
import com.isuwang.soa.core.Service;

/**
 *
 **/
@Service(version = "1.0.0")
@Processor(className = "com.isuwang.soa.transaction.api.GlobalTransactionServiceCodec$Processor")
public interface GlobalTransactionService {

    /**
     *
     **/
    com.isuwang.soa.transaction.api.domain.TGlobalTransaction create(com.isuwang.soa.transaction.api.domain.TGlobalTransaction globalTransaction) throws com.isuwang.soa.core.SoaException;

    /**
     *
     **/
    void update(Integer globalTransactionId, com.isuwang.soa.transaction.api.domain.TGlobalTransactionsStatus status) throws com.isuwang.soa.core.SoaException;

}
        