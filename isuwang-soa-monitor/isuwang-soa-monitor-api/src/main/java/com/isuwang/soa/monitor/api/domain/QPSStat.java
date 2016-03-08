package com.isuwang.soa.monitor.api.domain;

import java.util.Optional;

/**
 * QPS Stat
 **/
public class QPSStat {

    /**
     * 时间间隔:单位分钟
     **/
    public Integer period;

    public Integer getPeriod() {
        return this.period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }


    /**
     * 调用次数
     **/
    public Integer callCount;

    public Integer getCallCount() {
        return this.callCount;
    }

    public void setCallCount(Integer callCount) {
        this.callCount = callCount;
    }


    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"").append("period").append("\":").append(this.period).append(",");
        stringBuilder.append("\"").append("callCount").append("\":").append(this.callCount).append(",");

        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        stringBuilder.append("}");

        return stringBuilder.toString();
    }
}
      