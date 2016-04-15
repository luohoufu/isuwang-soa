package com.isuwang.soa.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.core.metadata.Service;
import com.isuwang.soa.remoting.fake.json.JSONPost;
import com.isuwang.soa.remoting.fake.metadata.MetadataClient;
import com.isuwang.soa.remoting.filter.LoadBalanceFilter;
import com.isuwang.soa.transaction.api.domain.*;
import com.isuwang.soa.transaction.db.action.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
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
        if (working.get())
            return;

        working.set(true);

        try {
            LOGGER.info("--- 定时事务管理器开始 ---");

            List<TGlobalTransaction> globalTransactionList = new GlobalTransactionsFindAction().execute();

            LOGGER.info("全局事务数量:{} 编号集合:{}", globalTransactionList.size(), globalTransactionList.stream().map(gt -> gt.getId()).collect(toList()));

            for (TGlobalTransaction globalTransaction : globalTransactionList) {

                globalTransaction = new GlobalTransactionFindByIdAction(globalTransaction.getId()).execute();
                if (globalTransaction.getStatus() != TGlobalTransactionsStatus.Fail && globalTransaction.getStatus() != TGlobalTransactionsStatus.PartiallyRollback)
                    continue;

                List<TGlobalTransactionProcess> transactionProcessList = new GlobalTransactionProcessFindAction(globalTransaction.getId()).execute();

                LOGGER.info("全局事务编号:{} 事务过程数量:{} 事务过程编号集合:{}", globalTransaction.getId(), transactionProcessList.size(), transactionProcessList.stream().map(gt -> gt.getId()).collect(toList()));

                if (transactionProcessList.isEmpty())
                    continue;

                int i = 0;
                for (; i < transactionProcessList.size(); i++) {
                    TGlobalTransactionProcess process = transactionProcessList.get(i);

                    LOGGER.info("全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 事务过程原状态:{} 事务过程期望状态:{} 动作:开始处理",
                            globalTransaction.getId(), process.getId(), process.getTransactionSequence(),
                            process.getStatus().name(), TGlobalTransactionProcessExpectedStatus.HasRollback.name());

                    if (process.getNextRedoTime().after(new Date())) {
                        LOGGER.info("全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 未到下次处理时间，跳出", globalTransaction.getId(), process.getId(), process.getTransactionSequence());
                        break;
                    }

                    //更新process的期望状态为“已回滚”
                    if (process.getExpectedStatus() != TGlobalTransactionProcessExpectedStatus.HasRollback)
                        new GlobalTransactionProcessExpectedStatusUpdateAction(process.getId(), TGlobalTransactionProcessExpectedStatus.HasRollback).execute();

                    String responseJson;
                    Service service = null;
                    //call roll back method
                    try {
                        //获取服务的metadata
                        String metadata = new MetadataClient(process.getServiceName(), process.getVersionName()).getServiceMetadata();
                        if (metadata != null) {
                            try (StringReader reader = new StringReader(metadata)) {
                                service = JAXB.unmarshal(reader, Service.class);
                            }
                        }

                        //获取服务的ip和端口
                        JSONPost jsonPost = null;
                        String callerInfo = LoadBalanceFilter.getCallerInfo(process.getServiceName(), process.getVersionName(), process.getRollbackMethodName());
                        if (callerInfo != null) {

                            String[] infos = callerInfo.split(":");
                            jsonPost = new JSONPost(infos[0], Integer.valueOf(infos[1]), false);

                        } else if (SoaSystemEnvProperties.SOA_REMOTING_MODE.equals("local")) {
                            jsonPost = new JSONPost(SoaSystemEnvProperties.SOA_SERVICE_IP, SoaSystemEnvProperties.SOA_SERVICE_PORT, false);
                        }

                        //构造请求参数，调用回滚方法
                        Map<String, Object> map = new HashMap<>();
                        Map<String, Object> request = new HashMap<>();
                        request.put("requestJson", process.getRequestJson());
                        request.put("responseJson", process.getResponseJson());
                        request.put("gtp", process);
                        map.put("gtp_request", request);

                        StringWriter requestWriter = new StringWriter();
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writeValue(requestWriter, map);

                        SoaHeader header = new SoaHeader();
                        header.setServiceName(process.getServiceName());
                        header.setVersionName(process.getVersionName());
                        header.setMethodName(process.getRollbackMethodName());
                        header.setCallerFrom(Optional.of("GlobalTransactionManager"));
                        header.setTransactionId(Optional.of(process.getTransactionId()));
                        header.setTransactionSequence(Optional.of(process.getTransactionSequence()));

                        responseJson = jsonPost.callServiceMethod(header, requestWriter.toString(), service);

                        //更新事务过程表为已回滚
                        new GlobalTransactionProcessUpdateAction(process.getId(), responseJson, TGlobalTransactionProcessStatus.HasRollback).execute();

                        LOGGER.info("全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 事务过程原状态:{} 事务过程期望状态:{} 动作:已完成",
                                globalTransaction.getId(), process.getId(), process.getTransactionSequence(),
                                process.getStatus().name(), TGlobalTransactionProcessExpectedStatus.HasRollback.name());
                    } catch (Exception e) {
                        LOGGER.info("全局事务编号:{} 事务过程编号:{} 事务过程序号:{} 事务过程原状态:{} 事务过程期望状态:{} 动作:异常({})",
                                globalTransaction.getId(), process.getId(), process.getTransactionSequence(),
                                process.getStatus().name(), TGlobalTransactionProcessExpectedStatus.HasRollback.name(), e.getMessage());

                        //更新事务过程表的重试次数和下次重试时间
                        new GlobalTransactionProcessUpdateAfterRollbackFail(process.getId()).execute();

                        LOGGER.error(e.getMessage(), e);
                        break;
                    }

                }

                if (i == 0) {
                    LOGGER.info("全局事务编号:{} 跳过", globalTransaction.getId());
                    continue;
                } else if (i == transactionProcessList.size()) {
                    //已回滚
                    new GlobalTransactionUpdateAction(globalTransaction.getId(), i > 0 ? transactionProcessList.get(i - 1).getTransactionSequence() : 0, TGlobalTransactionsStatus.HasRollback).execute();

                    LOGGER.info("全局事务编号:{} 已完成", globalTransaction.getId());
                } else {
                    //部分回滚
                    new GlobalTransactionUpdateAction(globalTransaction.getId(), i >= 0 ? transactionProcessList.get(i).getTransactionSequence() : 0, TGlobalTransactionsStatus.PartiallyRollback).execute();

                    LOGGER.info("全局事务编号:{} 部分完成", globalTransaction.getId());
                }
            }
            LOGGER.info("--- 定时事务管理器结束 ---");
        } finally {
            working.set(false);
        }
    }
}
