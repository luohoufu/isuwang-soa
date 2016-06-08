
package com.isuwang.dapeng.monitor.api.service;

import com.isuwang.dapeng.core.Processor;
import com.isuwang.dapeng.core.Service;
import com.isuwang.dapeng.core.SoaException;
import com.isuwang.dapeng.monitor.api.domain.DataSourceStat;
import com.isuwang.dapeng.monitor.api.domain.PlatformProcessData;
import com.isuwang.dapeng.monitor.api.domain.QPSStat;

/**
 * 监控服务
 **/
@Service(version = "1.0.0")
@Processor(className = "com.isuwang.dapeng.monitor.api.MonitorServiceCodec$Processor")
public interface MonitorService {

    /**
     * 上送QPS信息
     **/
    void uploadQPSStat(java.util.List<QPSStat> qpsStats) throws SoaException;

    /**
     * 上送平台处理数据
     **/
    void uploadPlatformProcessData(java.util.List<PlatformProcessData> platformProcessDatas) throws SoaException;

    /**
     * 上送DataSource信息
     **/
    void uploadDataSourceStat(java.util.List<DataSourceStat> dataSourceStat) throws SoaException;

}
        