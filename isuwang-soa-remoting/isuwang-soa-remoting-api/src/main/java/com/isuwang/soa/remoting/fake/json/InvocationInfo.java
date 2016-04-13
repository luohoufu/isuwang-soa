package com.isuwang.soa.remoting.fake.json;

import java.io.Serializable;

/**
 * 调用信息
 *
 * @author craneding
 * @date 15/4/26
 */
public class InvocationInfo implements Serializable {

    private static final long serialVersionUID = 4758486289693322574L;

    private DataInfo dataInfo;

    private String responseData;

    private boolean multiplexed;

    /**
     * 返回的数据格式
     */
    private String produce;

    public String getProduce() {
        return produce;
    }

    public void setProduce(String produce) {
        this.produce = produce;
    }

    public DataInfo getDataInfo() {
        return dataInfo;
    }

    public void setDataInfo(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public boolean isMultiplexed() {
        return multiplexed;
    }

    public void setMultiplexed(boolean multiplexed) {
        this.multiplexed = multiplexed;
    }
}
