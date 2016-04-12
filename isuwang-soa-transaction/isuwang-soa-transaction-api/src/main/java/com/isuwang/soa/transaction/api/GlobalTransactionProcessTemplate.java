package com.isuwang.soa.transaction.api;

import com.google.gson.Gson;
import com.isuwang.soa.core.InvocationContext;
import com.isuwang.soa.core.SoaException;
import com.isuwang.soa.transaction.api.domain.TGlobalTransactionProcess;
import com.isuwang.soa.transaction.api.domain.TGlobalTransactionProcessExpectedStatus;
import com.isuwang.soa.transaction.api.domain.TGlobalTransactionProcessStatus;
import com.isuwang.soa.transaction.api.service.GlobalTransactionProcessService;
import org.apache.thrift.TException;

import java.util.Date;

/**
 * Soa Transactional Process Template
 *
 * @author craneding
 * @date 16/4/11
 */
public class GlobalTransactionProcessTemplate {

    public <T> T execute(GlobalTransactionCallback<T> action) throws TException {
        final GlobalTransactionProcessService service = GlobalTransactionFactory.getGlobalTransactionProcessService();

        TGlobalTransactionProcess transactionProcess = null;

        boolean success = false, unknown = false;
        T result = null;

        try {
            InvocationContext context = InvocationContext.Factory.getCurrentInstance();

            transactionProcess = new TGlobalTransactionProcess();
            transactionProcess.setCreatedAt(new Date());
            transactionProcess.setCreatedBy(0);
            transactionProcess.setExpectedStatus(TGlobalTransactionProcessExpectedStatus.Success);
            transactionProcess.setMethodName(context.getHeader().getMethodName());
            transactionProcess.setRequestJson(context.getHeader().toString());
            transactionProcess.setRollbackMethodName(context.getHeader().getMethodName() + "_rollback");
            transactionProcess.setServiceName(context.getHeader().getServiceName());
            transactionProcess.setStatus(TGlobalTransactionProcessStatus.New);
            transactionProcess.setTransactionId(context.getHeader().getTransactionId().get());
            transactionProcess.setTransactionSequence(context.getHeader().getTransactionSequence().get());
            transactionProcess.setVersionName(context.getHeader().getVersionName());

            transactionProcess = service.create(transactionProcess);

            result = action.doInTransaction();

            success = true;

            return result;
        } catch (SoaException e) {
            switch (e.getCode()) {
                case "AA98":// 连接失败
                    unknown = false;
                    break;
                case "AA96":// 超时
                case "9999":// 未知
                    unknown = true;
                default:// 明确错误
                    unknown = false;
                    break;
            }

            throw e;
        } finally {
            final TGlobalTransactionProcessStatus status = success ? TGlobalTransactionProcessStatus.Success : (unknown ? TGlobalTransactionProcessStatus.Unknown : TGlobalTransactionProcessStatus.Fail);

            if (transactionProcess.getId() != null) {
                service.update(transactionProcess.getId(), result == null ? null : new Gson().toJson(result), status);
            }
        }
    }

}
