package com.isuwang.dapeng.transaction.db.dbc;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Created by tangliu on 16-02-18.
 */
@Service
public class DbcResources {

    public static DataSource transactionDataSorce;

    @Resource(name = "transaction_dataSource")
    public void setTransactionDataSorce(DataSource dataSource) {
        DbcResources.transactionDataSorce = dataSource;
    }

}
