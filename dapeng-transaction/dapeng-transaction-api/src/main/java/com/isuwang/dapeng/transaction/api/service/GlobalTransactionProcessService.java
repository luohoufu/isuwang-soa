
package com.isuwang.dapeng.transaction.api.service;

import com.isuwang.dapeng.core.SoaException;
import com.isuwang.dapeng.core.Processor;
import com.isuwang.dapeng.core.Service;
import com.isuwang.dapeng.transaction.api.domain.TGlobalTransactionProcess;
import com.isuwang.dapeng.transaction.api.domain.TGlobalTransactionProcessStatus;

/**
 *
 **/
@Service(version = "1.0.0")
@Processor(className = "com.isuwang.dapeng.transaction.api.GlobalTransactionProcessServiceCodec$Processor")
public interface GlobalTransactionProcessService {

    /**
     *
     **/
    TGlobalTransactionProcess create(TGlobalTransactionProcess globalTransactionProcess) throws SoaException;

    /**
     *
     **/
    void update(Integer globalTransactionProcessId, String responseJson, TGlobalTransactionProcessStatus status) throws SoaException;

}
        