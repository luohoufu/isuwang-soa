package com.isuwang.soa.core;

import java.util.Optional;

/**
 * Created by tangliu on 2016/3/15.
 */
public class HeaderProxy implements InvocationContext.Factory.ISoaHeaderProxy {

    String callerFrom;

    Integer customerId;

    String customerName;

    Integer operatorId;

    String operatorName;

    public void setCallerFrom(String callerFrom) {
        this.callerFrom = callerFrom;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    @Override
    public Optional<String> callerFrom() {
        return Optional.of(callerFrom);
    }

    @Override
    public Optional<Integer> customerId() {
        return Optional.of(customerId);
    }

    @Override
    public Optional<String> customerName() {
        return Optional.of(customerName);
    }

    @Override
    public Optional<Integer> operatorId() {
        return Optional.of(operatorId);
    }

    @Override
    public Optional<String> operatorName() {
        return Optional.of(operatorName);
    }
}
