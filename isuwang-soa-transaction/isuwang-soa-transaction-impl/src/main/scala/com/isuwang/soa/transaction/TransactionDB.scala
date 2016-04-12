package com.isuwang.soa.transaction

import javax.sql.DataSource

import com.isuwang.scala.dbc.DB
import com.isuwang.soa.transaction.db.dbc.DbcResources

/**
  * Created by tangliu on 2016/3/25.
  */
object TransactionDB extends DB {

  override def withDataSource(): DataSource = {
    DbcResources.kuaisuwangDataSorce
  }
}
