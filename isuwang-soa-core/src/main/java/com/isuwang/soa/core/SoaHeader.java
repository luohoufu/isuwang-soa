package com.isuwang.soa.core;

import java.util.Optional;

/**
 * Created by tangliu on 2016/1/11.
 */
public class SoaHeader {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 版本号
     */
    private String versionName;

    /**
     * 调用源
     */
    private Optional<String> callerFrom = Optional.empty();

    /**
     * 返回码
     */
    private Optional<String> respCode = Optional.empty();

    /**
     * 返回信息
     */
    private Optional<String> respMessage = Optional.empty();

    /**
     * 调用源ip
     */
    private Optional<String> callerIp = Optional.empty();

    /**
     * 操作人编号
     */
    private Optional<Integer> operatorId = Optional.empty();

    /**
     * 操作人名称
     */
    private Optional<String> operatorName = Optional.empty();

    /**
     * 客户编号
     */
    private Optional<Integer> customerId = Optional.empty();

    /**
     * 客户名称
     */
    private Optional<String> customerName = Optional.empty();


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Optional<String> getCallerIp() {
        return callerIp;
    }

    public void setCallerIp(Optional<String> callerIp) {
        this.callerIp = callerIp;
    }

    public Optional<Integer> getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Optional<Integer> operatorId) {
        this.operatorId = operatorId;
    }

    public Optional<String> getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(Optional<String> operatorName) {
        this.operatorName = operatorName;
    }

    public Optional<Integer> getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Optional<Integer> customerId) {
        this.customerId = customerId;
    }

    public Optional<String> getCustomerName() {
        return customerName;
    }

    public void setCustomerName(Optional<String> customerName) {
        this.customerName = customerName;
    }

    public Optional<String> getRespCode() {
        return respCode;
    }

    public void setRespCode(Optional<String> respCode) {
        this.respCode = respCode;
    }

    public Optional<String> getRespMessage() {
        return respMessage;
    }

    public void setRespMessage(Optional<String> respMessage) {
        this.respMessage = respMessage;
    }

    public Optional<String> getCallerFrom() {
        return callerFrom;
    }

    public void setCallerFrom(Optional<String> callerFrom) {
        this.callerFrom = callerFrom;
    }
}
