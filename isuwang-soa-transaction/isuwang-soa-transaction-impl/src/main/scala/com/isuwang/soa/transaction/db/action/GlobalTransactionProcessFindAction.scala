package com.isuwang.soa.transaction.db.action

import com.isuwang.scala.dbc.Action
import com.isuwang.scala.dbc.Assert._
import com.isuwang.scala.dbc.Implicit._
import com.isuwang.soa.transaction.TransactionDB._
import com.isuwang.soa.transaction.api.domain.TGlobalTransactionProcess
import com.isuwang.soa.transaction.db.domain.GlobalTransactionProcess
import com.isuwang.soa.transaction.utils.ErrorCode
import wangzx.scala_commons.sql._

import scala.collection.JavaConversions._

/**
  * 查找所有的成功的或者未知的事务过程记录
  *
  *
  * Created by tangliu on 2016/4/13.
  */
class GlobalTransactionProcessFindAction(transactionId: Int) extends Action[java.util.List[TGlobalTransactionProcess]] {

  override def inputCheck: Unit = {

    assert(transactionId > 0, ErrorCode.INPUTERROR.getCode, "全局事务id错误")
  }

  override def action: java.util.List[TGlobalTransactionProcess] = {
    val selectSql =
      sql"""
         SELECT *, requestJson as request_json, responseJson as response_json
         FROM global_transaction_process
         WHERE transaction_id = ${transactionId} and (status = 2 OR status = 4) and next_redo_time < now()
         ORDER BY transaction_sequence DESC
       """
    rows[GlobalTransactionProcess](selectSql).toThrifts[TGlobalTransactionProcess]

  }

  override def postCheck: Unit = {}

  override def preCheck: Unit = {}
}
