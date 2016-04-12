package com.isuwang.soa.transaction.service;

import com.isuwang.soa.core.SoaException;
import com.isuwang.soa.transaction.api.domain.TGlobalTransactionProcess;
import com.isuwang.soa.transaction.api.domain.TGlobalTransactionProcessStatus;
import com.isuwang.soa.transaction.api.service.GlobalTransactionProcessService;
import com.isuwang.soa.transaction.db.action.GlobalTransactionProcessCreateAction;
import com.isuwang.soa.transaction.db.action.GlobalTransactionProcessUpdateAction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by tangliu on 2016/4/12.
 */
@Transactional(value = "globalTransaction", rollbackFor = Exception.class)
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
