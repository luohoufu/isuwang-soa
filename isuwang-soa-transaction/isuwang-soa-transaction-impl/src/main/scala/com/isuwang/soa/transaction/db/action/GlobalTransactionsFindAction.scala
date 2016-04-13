package com.isuwang.soa.transaction.db.action

import com.isuwang.scala.dbc.Action
import com.isuwang.scala.dbc.Implicit._
import com.isuwang.soa.transaction.TransactionDB._
import com.isuwang.soa.transaction.api.domain.TGlobalTransaction
import com.isuwang.soa.transaction.db.domain.GlobalTransaction
import wangzx.scala_commons.sql._

import scala.collection.JavaConversions._

/**
  * 查找所有的失败或部分回滚的全局事务记录
  *
  *
  * Created by tangliu on 2016/4/13.
  */
class GlobalTransactionsFindAction() extends Action[java.util.List[TGlobalTransaction]] {

  override def inputCheck: Unit = {}

  override def action: java.util.List[TGlobalTransaction] = {
    val selectSql =
      sql"""
         SELECT id, status, curr_sequence
         FROM global_transactions
         WHERE status = 3 or status = 5
       """
    rows[GlobalTransaction](selectSql).toThrifts[TGlobalTransaction]

  }

  override def postCheck: Unit = {}

  override def preCheck: Unit = {}
}
