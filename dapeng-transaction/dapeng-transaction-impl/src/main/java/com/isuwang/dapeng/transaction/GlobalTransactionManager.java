package com.isuwang.dapeng.transaction;

import com.isuwang.dapeng.core.SoaHeader;
import com.isuwang.dapeng.core.SoaSystemEnvProperties;
import com.isuwang.dapeng.core.helper.MasterHelper;
import com.isuwang.dapeng.core.metadata.Service;
import com.isuwang.dapeng.remoting.fake.json.JSONPost;
import com.isuwang.dapeng.remoting.fake.metadata.MetadataClient;
import com.isuwang.dapeng.remoting.filter.LoadBalanceFilter;
import com.isuwang.dapeng.transaction.api.domain.*;
import com.isuwang.dapeng.transaction.db.action.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.toList;

/**
 * Global Transaction Manager
 *
 * @author craneding
 * @date 16/4/12
 */
public class GlobalTransactionManager {

    Logger LOGGER = LoggerFactory.getLogger(GlobalTransactionManager.class);

    private AtomicBoolean working = new AtomicBoolean(false);

    @Transactional(value = "globalTransaction", rollbackFor = Exception.class)
    public void doJob() {

        if (!MasterHelper.isMaster("com.isuwang.dapeng.transaction.api.service.GlobalTransactionService", "1.0.0")) {
            LOGGER.info("--- 定时事务管理器不是Master，跳过 ---");
            return;
        }
        if (working.get())
            return;

        working.set(true);

        try {
            LOGGER.info("--- 定时事务管理器开始 ---");

            /**
             * 处理全局事务状态为失败或者部分回滚的记录，这些全局事务下的成功的子事务过程应该回滚
             */
            List<TGlobalTransaction> globalTransactionList = new GlobalTransactionsFindAction().execute();

            LOGGER.info("需回滚全局事务数量:{} 编号集合:{}", globalTransactionList.size(), globalTransactionList.stream().map(gt -> gt.getId()).collect(toList()));

            for (TGlobalTransaction globalTransaction : globalTransactionList) {

                globalTransaction = new GlobalTransactionFindByIdAction(globalTransaction.getId()).execute();
                if (globalTransaction.getStatus() != TGlobalTransactionsStatus.Fail && globalTransaction.getStatus() != TGlobalTransactionsStatus.PartiallyRollback)
                    continue;

                List<TGlobalTransactionProcess> transactionProcessList = new GlobalTransactionProcessFindAction(globalTransaction.getId()).execute();

                LOGGER.info("需回滚全局事务编号:{} 事务过程数量:{} 事务过程编号集合:{}", globalTransaction.getId(), transactionProcessList.size(), transactionProcessList.stream().map(gt -> gt.getId()).collect(toList()));

                if (transactionProcessList.isEmpty()) {
                    //如果事务过程为空，则说明该全局事务不需要再做处理，直接修改状态
                    new GlobalTransactionUpdateAction(globalTransaction.getId(), 0, TGlobalTransactionsStatus.HasRollback).execute();
                    continue;
                }

                int i = 0;
                for (; i < transactionProcessList.size(); i++) {
                    TGlobalTransactionProcess process = transactionProcessList.get(i);

                    LOGGER.info("需回滚全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 事务过程原状态:{} 事务过程期望状态:{} 动作:开始处理",
                            globalTransaction.getId(), process.getId(), process.getTransactionSequence(),
                            process.getStatus().name(), TGlobalTransactionProcessExpectedStatus.HasRollback.name());

                    if (process.getNextRedoTime().after(new Date())) {
                        LOGGER.info("需回滚全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 未到下次处理时间，跳出", globalTransaction.getId(), process.getId(), process.getTransactionSequence());
                        break;
                    }

                    //更新process的期望状态为“已回滚”
                    if (process.getExpectedStatus() != TGlobalTransactionProcessExpectedStatus.HasRollback)
                        new GlobalTransactionProcessExpectedStatusUpdateAction(process.getId(), TGlobalTransactionProcessExpectedStatus.HasRollback).execute();

                    String responseJson;
                    //call roll back method
                    try {
                        responseJson = callServiceMethod(process, true);

                        //更新事务过程表为已回滚
                        new GlobalTransactionProcessUpdateAction(process.getId(), responseJson, TGlobalTransactionProcessStatus.HasRollback).execute();

                        LOGGER.info("需回滚全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 事务过程原状态:{} 事务过程期望状态:{} 动作:已完成",
                                globalTransaction.getId(), process.getId(), process.getTransactionSequence(),
                                process.getStatus().name(), TGlobalTransactionProcessExpectedStatus.HasRollback.name());
                    } catch (Exception e) {
                        LOGGER.info("需回滚全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 事务过程原状态:{} 事务过程期望状态:{} 动作:异常({})",
                                globalTransaction.getId(), process.getId(), process.getTransactionSequence(),
                                process.getStatus().name(), TGlobalTransactionProcessExpectedStatus.HasRollback.name(), e.getMessage());

                        //更新事务过程表的重试次数和下次重试时间
                        new GlobalTransactionProcessUpdateAfterRollbackFail(process.getId()).execute();

                        LOGGER.error(e.getMessage(), e);
                        break;
                    }

                }

                if (i == 0) {
                    LOGGER.info("需回滚全局事务编号:{} 跳过", globalTransaction.getId());
                    continue;
                } else if (i == transactionProcessList.size()) {
                    //已回滚
                    new GlobalTransactionUpdateAction(globalTransaction.getId(), i > 0 ? transactionProcessList.get(i - 1).getTransactionSequence() : 0, TGlobalTransactionsStatus.HasRollback).execute();

                    LOGGER.info("需回滚全局事务编号:{} 已完成", globalTransaction.getId());
                } else {
                    //部分回滚
                    new GlobalTransactionUpdateAction(globalTransaction.getId(), i >= 0 ? transactionProcessList.get(i).getTransactionSequence() : 0, TGlobalTransactionsStatus.PartiallyRollback).execute();

                    LOGGER.info("需回滚全局事务编号:{} 部分完成", globalTransaction.getId());
                }
            }


            /**
             * 处理所有全局事务状态为成功，但对应子过程中存在失败状态的记录，这种情况下，应该对子过程顺序做向前处理
             */
            globalTransactionList = new GlobalSucTransactionWithFailProcessFindAction().execute();

            LOGGER.info("需向前全局事务数量:{} 编号集合:{}", globalTransactionList.size(), globalTransactionList.stream().map(gt -> gt.getId()).collect(toList()));

            for (TGlobalTransaction globalTransaction : globalTransactionList) {

                globalTransaction = new GlobalTransactionFindByIdAction(globalTransaction.getId()).execute();
                if (globalTransaction.getStatus() != TGlobalTransactionsStatus.Success)
                    continue;

                List<TGlobalTransactionProcess> transactionProcessList = new FailedTransactionProcessFindAction(globalTransaction.getId()).execute();

                LOGGER.info("需向前全局事务编号:{} 事务过程数量:{} 事务过程编号集合:{}", globalTransaction.getId(), transactionProcessList.size(), transactionProcessList.stream().map(gt -> gt.getId()).collect(toList()));

                if (transactionProcessList.isEmpty())
                    continue;

                int i = 0;
                for (; i < transactionProcessList.size(); i++) {
                    TGlobalTransactionProcess process = transactionProcessList.get(i);

                    LOGGER.info("需向前全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 事务过程原状态:{} 事务过程期望状态:{} 动作:开始处理",
                            globalTransaction.getId(), process.getId(), process.getTransactionSequence(),
                            process.getStatus().name(), TGlobalTransactionProcessExpectedStatus.Success.name());

                    if (process.getNextRedoTime().after(new Date())) {
                        LOGGER.info("需向前全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 未到下次处理时间，跳出", globalTransaction.getId(), process.getId(), process.getTransactionSequence());
                        break;
                    }

                    //更新process的期望状态为“成功”
                    if (process.getExpectedStatus() != TGlobalTransactionProcessExpectedStatus.Success)
                        new GlobalTransactionProcessExpectedStatusUpdateAction(process.getId(), TGlobalTransactionProcessExpectedStatus.Success).execute();

                    String responseJson;
                    //call method
                    try {
                        responseJson = callServiceMethod(process, false);

                        //更新事务过程表为成功
                        new GlobalTransactionProcessUpdateAction(process.getId(), responseJson, TGlobalTransactionProcessStatus.Success).execute();

                        LOGGER.info("需向前全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 事务过程原状态:{} 事务过程期望状态:{} 动作:已完成",
                                globalTransaction.getId(), process.getId(), process.getTransactionSequence(),
                                process.getStatus().name(), TGlobalTransactionProcessExpectedStatus.Success.name());
                    } catch (Exception e) {
                        LOGGER.info("需向前全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 事务过程原状态:{} 事务过程期望状态:{} 动作:异常({})",
                                globalTransaction.getId(), process.getId(), process.getTransactionSequence(),
                                process.getStatus().name(), TGlobalTransactionProcessExpectedStatus.Success.name(), e.getMessage());

                        //更新事务过程表的重试次数和下次重试时间
                        new GlobalTransactionProcessUpdateAfterRollbackFail(process.getId()).execute();

                        LOGGER.error(e.getMessage(), e);
                        break;
                    }

                }

                if (i == 0) {
                    LOGGER.info("需向前全局事务编号:{} 跳过", globalTransaction.getId());
                    continue;
                } else if (i == transactionProcessList.size()) {
                    //已经全部向前
//                    new GlobalTransactionUpdateAction(globalTransaction.getId(), i > 0 ? transactionProcessList.get(i - 1).getTransactionSequence() : 0, TGlobalTransactionsStatus.Success).execute();
                    LOGGER.info("需向前全局事务编号:{} 已完成", globalTransaction.getId());
                } else {
                    //部分向前，不更新状态
//                    new GlobalTransactionUpdateAction(globalTransaction.getId(), i >= 0 ? transactionProcessList.get(i).getTransactionSequence() : 0, TGlobalTransactionsStatus.Success).execute();
                    LOGGER.info("需向前全局事务编号:{} 部分完成", globalTransaction.getId());
                }
            }

            LOGGER.info("--- 定时事务管理器结束 ---");
        } finally {
            working.set(false);
        }
    }


    private static String callServiceMethod(TGlobalTransactionProcess process, boolean rollbackOrForward) throws Exception {

        String responseJson;
        Service service = null;

        //获取服务的metadata
        String metadata = new MetadataClient(process.getServiceName(), process.getVersionName()).getServiceMetadata();
        if (metadata != null) {
            try (StringReader reader = new StringReader(metadata)) {
                service = JAXB.unmarshal(reader, Service.class);
            }
        }

        //获取服务的ip和端口
        JSONPost jsonPost = null;

        String callerInfo;
        if (rollbackOrForward)
            callerInfo = LoadBalanceFilter.getCallerInfo(process.getServiceName(), process.getVersionName(), process.getRollbackMethodName());
        else
            callerInfo = LoadBalanceFilter.getCallerInfo(process.getServiceName(), process.getVersionName(), process.getMethodName());

        if (callerInfo != null) {

            String[] infos = callerInfo.split(":");
            jsonPost = new JSONPost(infos[0], Integer.valueOf(infos[1]), false);

        } else if (SoaSystemEnvProperties.SOA_REMOTING_MODE.equals("local")) {
            jsonPost = new JSONPost(SoaSystemEnvProperties.SOA_SERVICE_IP, SoaSystemEnvProperties.SOA_SERVICE_PORT, false);
        }

        SoaHeader header = new SoaHeader();
        header.setServiceName(process.getServiceName());
        header.setVersionName(process.getVersionName());
        header.setMethodName(rollbackOrForward ? process.getRollbackMethodName() : process.getMethodName());
        header.setCallerFrom(Optional.of("GlobalTransactionManager"));
        header.setTransactionId(Optional.of(process.getTransactionId()));
        header.setTransactionSequence(Optional.of(process.getTransactionSequence()));

        if (rollbackOrForward)
            responseJson = jsonPost.callServiceMethod(header, "", service);
        else
            responseJson = jsonPost.callServiceMethod(header, process.getRequestJson(), service);

        return responseJson;
    }
}
