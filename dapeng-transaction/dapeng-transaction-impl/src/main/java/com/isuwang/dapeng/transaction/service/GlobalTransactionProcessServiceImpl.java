package com.isuwang.dapeng.transaction.service;

import com.isuwang.dapeng.core.SoaException;
import com.isuwang.dapeng.transaction.api.domain.TGlobalTransactionProcess;
import com.isuwang.dapeng.transaction.api.domain.TGlobalTransactionProcessStatus;
import com.isuwang.dapeng.transaction.api.service.GlobalTransactionProcessService;
import com.isuwang.dapeng.transaction.db.action.GlobalTransactionProcessUpdateAction;
import com.isuwang.dapeng.transaction.db.action.GlobalTransactionProcessCreateAction;
import com.isuwang.dapeng.transaction.db.action.GlobalTransactionProcessUpdateAction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by tangliu on 2016/4/12.
 */
@Transactional(value = "globalTransaction", rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
public class GlobalTransactionProcessServiceImpl implements GlobalTransactionProcessService {

    @Override
    public TGlobalTransactionProcess create(TGlobalTransactionProcess globalTransactionProcess) throws SoaException {
        return new GlobalTransactionProcessCreateAction(globalTransactionProcess).execute();
    }

    @Override
    public void update(Integer globalTransactionProcessId, String responseJson, TGlobalTransactionProcessStatus status) throws SoaException {
        new GlobalTransactionProcessUpdateAction(globalTransactionProcessId, responseJson, status).execute();
    }
}
