package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.TransactionContext;

/**
 * Created by tangliu on 2016/2/1.
 */
public class Task {

    private String serviceName;

    private String versionName;

    private String methodName;

    private long startTime;

    private String callerFrom;

    private String callerIp;

    private Integer seqid;

    private Integer operatorId;

    private String operatorName;

    private Integer customerId;

    private String customerName;

    private Thread currentThread;


    public Task(TransactionContext context) {

        this.startTime = System.currentTimeMillis();
        this.seqid = context.getSeqid();

        SoaHeader soaHeader = context.getHeader();
        this.serviceName = soaHeader.getServiceName();
        this.versionName = soaHeader.getVersionName();
        this.methodName = soaHeader.getMethodName();
        this.callerFrom = soaHeader.getCallerFrom().isPresent() ? soaHeader.getCallerFrom().get() : null;
        this.callerIp = soaHeader.getCallerIp().isPresent() ? soaHeader.getCallerIp().get() : null;
        this.operatorId = soaHeader.getOperatorId().isPresent() ? soaHeader.getOperatorId().get() : null;
        this.operatorName = soaHeader.getOperatorName().isPresent() ? soaHeader.getOperatorName().get() : null;
        this.customerId = soaHeader.getCustomerId().isPresent() ? soaHeader.getCustomerId().get() : null;
        this.customerName = soaHeader.getCustomerName().isPresent() ? soaHeader.getCustomerName().get() : null;

        this.currentThread = Thread.currentThread();
    }

    public Integer getSeqid() {
        return seqid;
    }

    public void setSeqid(Integer seqid) {
        this.seqid = seqid;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getCallerFrom() {
        return callerFrom;
    }

    public void setCallerFrom(String callerFrom) {
        this.callerFrom = callerFrom;
    }

    public String getCallerIp() {
        return callerIp;
    }

    public void setCallerIp(String callerIp) {
        this.callerIp = callerIp;
    }

    public Thread getCurrentThread() {
        return currentThread;
    }

    public void setCurrentThread(Thread currentThread) {
        this.currentThread = currentThread;
    }
}
