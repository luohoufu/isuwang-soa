namespace java com.isuwang.soa.monitor.api.domain

/**
* QPS Stat
**/
struct QPSStat {

    /**
    * 时间间隔:单位分钟
    **/
    1:i32 period,

    /**
    * 调用次数
    **/
    2:i32 callCount

}

/**
* 平台处理数据
**/
struct PlatformProcessData {

    /**
    * 时间间隔:单位分钟
    **/
    1:i32 period,

    /**
    * 统计分析时间(时间戳)
    **/
    2:i64 analysisTime,

    /**
    * 服务名称
    **/
    3:string serviceName,

    /**
    * 方法名称
    **/
    4:string methodName,

    /**
    * 版本号
    **/
    5:string versionName,

    /**
    * 服务器IP
    **/
    6:string serverIP,

    /**
    * 服务器端口
    **/
    7:i32 serverPort,

    /**
    * 平台最小耗时(单位:毫秒)
    **/
    8:i64 pMinTime,

    /**
    * 平台最大耗时(单位:毫秒)
    **/
    9:i64 pMaxTime,

    /**
    * 平台平均耗时(单位:毫秒)
    **/
    10:i64 pAverageTime,

    /**
    * 平台总耗时(单位:毫秒)
    **/
    11:i64 pTotalTime,

    /**
    * 接口服务最小耗时(单位:毫秒)
    **/
    12:i64 iMinTime,

    /**
    * 接口服务最大耗时(单位:毫秒)
    **/
    13:i64 iMaxTime,

    /**
    * 接口服务平均耗时(单位:毫秒)
    **/
    14:i64 iAverageTime,

    /**
    * 接口服务总耗时(单位:毫秒)
    **/
    15:i64 iTotalTime,

    /**
    * 总调用次数
    **/
    16:i32 totalCalls,

    /**
    * 成功调用次数
    **/
    17:i32 succeedCalls,

    /**
    * 失败调用次数
    **/
    18:i32 failCalls,

    /**
    * 请求的流量(单位:字节)
    **/
    19:i32 requestFlow,

    /**
    * 响应的流量(单位:字节)
    **/
    20:i32 responseFlow

}