package com.isuwang.soa.transaction.db.dbc;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Created by tangliu on 16-02-18.
 */
@Service
public class DbcResources {

    public static DataSource kuaisuwangDataSorce;

    @Resource(name = "kuangsuwang_dataSource")
    public void setKuaisuwangDataSorce(DataSource dataSource) {
        DbcResources.kuaisuwangDataSorce = dataSource;
    }

}
