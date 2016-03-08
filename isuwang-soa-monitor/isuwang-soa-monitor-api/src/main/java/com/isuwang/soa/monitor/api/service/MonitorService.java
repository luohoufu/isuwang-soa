
package com.isuwang.soa.monitor.api.service;

import com.isuwang.soa.core.Processor;
import com.isuwang.soa.core.Service;

/**
 * 监控服务
 **/
@Service(version = "1.0.0")
@Processor(className = "com.isuwang.soa.monitor.api.MonitorServiceCodec$Processor")
public interface MonitorService {

    /**
     * 上送QPS信息
     **/
    void uploadQPSStat(com.isuwang.soa.monitor.api.domain.QPSStat qpsStat) throws com.isuwang.soa.core.SoaException;

    /**
     * 上送平台处理数据
     **/
    void uploadPlatformProcessData(java.util.List<com.isuwang.soa.monitor.api.domain.PlatformProcessData> platformProcessDatas) throws com.isuwang.soa.core.SoaException;

}
        