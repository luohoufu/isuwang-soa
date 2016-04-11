package com.isuwang.soa.transaction.api;

import com.isuwang.soa.core.SoaException;

/**
 * Soa Transactional Process Template
 *
 * @author craneding
 * @date 16/4/11
 */
public class SoaTransactionalProcessTemplate {

    public <T> T execute(SoaTransactionalProcessCallback<T> action) throws SoaException {
        try {
            T result = action.doInTransactionProcess();

            return result;
        } catch (SoaException e) {
            /*
            switch (e.getErrCode()) {
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
            */

            throw e;
        } finally {

        }
    }

}
