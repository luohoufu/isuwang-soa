package com.isuwang.dapeng.transaction.service;

import com.isuwang.dapeng.core.SoaException;
import com.isuwang.dapeng.transaction.api.domain.TGlobalTransaction;
import com.isuwang.dapeng.transaction.api.domain.TGlobalTransactionsStatus;
import com.isuwang.dapeng.transaction.api.service.GlobalTransactionService;
import com.isuwang.dapeng.transaction.db.action.GlobalTransactionCreateAction;
import com.isuwang.dapeng.transaction.db.action.GlobalTransactionUpdateAction;
import com.isuwang.dapeng.transaction.db.action.GlobalTransactionCreateAction;
import com.isuwang.dapeng.transaction.db.action.GlobalTransactionUpdateAction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by tangliu on 2016/4/12.
 */
@Transactional(value = "globalTransaction", rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
public class GlobalTransactionServiceImpl implements GlobalTransactionService {

    @Override
    public TGlobalTransaction create(TGlobalTransaction globalTransaction) throws SoaException {
        return new GlobalTransactionCreateAction(globalTransaction).execute();
    }

    @Override
    public void update(Integer globalTransactionId, Integer currSequence, TGlobalTransactionsStatus status) throws SoaException {
        new GlobalTransactionUpdateAction(globalTransactionId, currSequence, status).execute();
    }
}
