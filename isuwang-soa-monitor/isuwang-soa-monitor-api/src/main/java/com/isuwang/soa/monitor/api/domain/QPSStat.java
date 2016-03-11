package com.isuwang.soa.monitor.api.domain;

/**
 * QPS Stat
 **/
public class QPSStat {

    /**
     * 时间间隔:单位秒
     **/
    private Integer period;

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
     * 调用次数
     **/
    private Integer callCount;

    public Integer getCallCount() {
        return this.callCount;
    }

    public void setCallCount(Integer callCount) {
        this.callCount = callCount;
    }


    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"").append("period").append("\":").append(this.period).append(",");
        stringBuilder.append("\"").append("analysisTime").append("\":").append(this.analysisTime).append(",");
        stringBuilder.append("\"").append("serverIP").append("\":\"").append(this.serverIP).append("\",");
        stringBuilder.append("\"").append("serverPort").append("\":").append(this.serverPort).append(",");
        stringBuilder.append("\"").append("callCount").append("\":").append(this.callCount).append(",");

        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        stringBuilder.append("}");

        return stringBuilder.toString();
    }
}
      