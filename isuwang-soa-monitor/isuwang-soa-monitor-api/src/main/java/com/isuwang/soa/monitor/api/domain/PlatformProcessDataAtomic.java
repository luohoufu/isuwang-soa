package com.isuwang.soa.monitor.api.domain;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by tangliu on 2016/3/9.
 */
public class PlatformProcessDataAtomic {

    /**
     * 时间间隔:单位分钟
     **/
    private Integer period;

    public AtomicLong getpMinTime() {
        return pMinTime;
    }

    public void setpMinTime(AtomicLong pMinTime) {
        this.pMinTime = pMinTime;
    }

    public AtomicLong getpMaxTime() {
        return pMaxTime;
    }

    public void setpMaxTime(AtomicLong pMaxTime) {
        this.pMaxTime = pMaxTime;
    }

    public long getpAverageTime() {
        return pAverageTime;
    }

    public void setpAverageTime(long pAverageTime) {
        this.pAverageTime = pAverageTime;
    }

    public AtomicLong getpTotalTime() {
        return pTotalTime;
    }

    public void setpTotalTime(AtomicLong pTotalTime) {
        this.pTotalTime = pTotalTime;
    }

    public AtomicLong getiMinTime() {
        return iMinTime;
    }

    public void setiMinTime(AtomicLong iMinTime) {
        this.iMinTime = iMinTime;
    }

    public AtomicLong getiMaxTime() {
        return iMaxTime;
    }

    public void setiMaxTime(AtomicLong iMaxTime) {
        this.iMaxTime = iMaxTime;
    }

    public Long getiAverageTime() {
        return iAverageTime;
    }

    public void setiAverageTime(Long iAverageTime) {
        this.iAverageTime = iAverageTime;
    }

    public AtomicLong getiTotalTime() {
        return iTotalTime;
    }

    public void setiTotalTime(AtomicLong iTotalTime) {
        this.iTotalTime = iTotalTime;
    }

    public AtomicInteger getTotalCalls() {
        return totalCalls;
    }

    public void setTotalCalls(AtomicInteger totalCalls) {
        this.totalCalls = totalCalls;
    }

    public AtomicInteger getSucceedCalls() {
        return succeedCalls;
    }

    public void setSucceedCalls(AtomicInteger succeedCalls) {
        this.succeedCalls = succeedCalls;
    }

    public AtomicInteger getFailCalls() {
        return failCalls;
    }

    public void setFailCalls(AtomicInteger failCalls) {
        this.failCalls = failCalls;
    }

    public AtomicInteger getRequestFlow() {
        return requestFlow;
    }

    public void setRequestFlow(AtomicInteger requestFlow) {
        this.requestFlow = requestFlow;
    }

    public AtomicInteger getResponseFlow() {
        return responseFlow;
    }

    public void setResponseFlow(AtomicInteger responseFlow) {
        this.responseFlow = responseFlow;
    }

    public Integer getPeriod() {

        return this.period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }


    /**
     * 统计分析时间(时间戳)
     **/
    private Long analysisTime;

    public Long getAnalysisTime() {
        return this.analysisTime;
    }

    public void setAnalysisTime(Long analysisTime) {
        this.analysisTime = analysisTime;
    }


    /**
     * 服务名称
     **/
    private String serviceName;

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


    /**
     * 方法名称
     **/
    private String methodName;

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    /**
     * 版本号
     **/
    private String versionName;

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }


    /**
     * 服务器IP
     **/
    private String serverIP;

    public String getServerIP() {
        return this.serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }


    /**
     * 服务器端口
     **/
    private Integer serverPort;

    public Integer getServerPort() {
        return this.serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }


    /**
     * 平台最小耗时(单位:毫秒)
     **/
    private AtomicLong pMinTime;

    /**
     * 平台最大耗时(单位:毫秒)
     **/
    private AtomicLong pMaxTime;


    /**
     * 平台平均耗时(单位:毫秒)
     **/
    private long pAverageTime;


    /**
     * 平台总耗时(单位:毫秒)
     **/
    private AtomicLong pTotalTime;


    /**
     * 接口服务最小耗时(单位:毫秒)
     **/
    private AtomicLong iMinTime;


    /**
     * 接口服务最大耗时(单位:毫秒)
     **/
    private AtomicLong iMaxTime;


    /**
     * 接口服务平均耗时(单位:毫秒)
     **/
    private Long iAverageTime;


    /**
     * 接口服务总耗时(单位:毫秒)
     **/
    private AtomicLong iTotalTime;


    /**
     * 总调用次数
     **/
    private AtomicInteger totalCalls;


    /**
     * 成功调用次数
     **/
    private AtomicInteger succeedCalls;


    /**
     * 失败调用次数
     **/
    private AtomicInteger failCalls;


    /**
     * 请求的流量(单位:字节)
     **/
    private AtomicInteger requestFlow;


    /**
     * 响应的流量(单位:字节)
     **/
    private AtomicInteger responseFlow;

}
