include "monitor_domain.thrift"

namespace java com.isuwang.soa.monitor.api.service

/**
* 监控服务
**/
service MonitorService {

    /**
    * 上送QPS信息
    **/
    void uploadQPSStat(1:monitor_domain.QPSStat qpsStat),

    /**
    * 上送平台处理数据
    **/
    void uploadPlatformProcessData(1:list<monitor_domain.PlatformProcessData> platformProcessDatas)

}
