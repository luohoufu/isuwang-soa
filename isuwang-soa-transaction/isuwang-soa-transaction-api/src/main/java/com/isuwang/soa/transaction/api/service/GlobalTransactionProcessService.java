
package com.isuwang.soa.transaction.api.service;

import com.isuwang.soa.core.Processor;
import com.isuwang.soa.core.Service;

/**
 *
 **/
@Service(version = "1.0.0")
@Processor(className = "com.isuwang.soa.transaction.api.GlobalTransactionProcessServiceCodec$Processor")
public interface GlobalTransactionProcessService {

    /**
     *
     **/
    com.isuwang.soa.transaction.api.domain.TGlobalTransactionProcess create(com.isuwang.soa.transaction.api.domain.TGlobalTransactionProcess globalTransactionProcess) throws com.isuwang.soa.core.SoaException;

    /**
     *
     **/
    void update(Integer globalTransactionProcessId, String responseJson, com.isuwang.soa.transaction.api.domain.TGlobalTransactionProcessStatus status) throws com.isuwang.soa.core.SoaException;

}
        