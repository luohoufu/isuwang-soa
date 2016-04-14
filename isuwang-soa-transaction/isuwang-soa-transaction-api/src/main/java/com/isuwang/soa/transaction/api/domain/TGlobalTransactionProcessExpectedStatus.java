package com.isuwang.soa.transaction.api.domain;

public enum TGlobalTransactionProcessExpectedStatus implements org.apache.thrift.TEnum {

    /**
     *
     **/
    Success(1),

    /**
     *
     **/
    HasRollback(2);


    private final int value;

    private TGlobalTransactionProcessExpectedStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static TGlobalTransactionProcessExpectedStatus findByValue(int value) {
        switch (value) {

            case 1:
                return Success;

            case 2:
                return HasRollback;

            default:
                return null;
        }
    }
}
      