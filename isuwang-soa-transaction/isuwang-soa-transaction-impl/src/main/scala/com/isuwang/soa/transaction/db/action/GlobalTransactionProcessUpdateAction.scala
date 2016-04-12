package com.isuwang.soa.transaction.db.action

import java.sql.Timestamp
import java.util.Date

import com.isuwang.scala.dbc.Action
import com.isuwang.scala.dbc.Assert._
import com.isuwang.soa.core.{SoaException, TransactionContext}
import com.isuwang.soa.transaction.TransactionDB._
import com.isuwang.soa.transaction.TransactionSQL
import com.isuwang.soa.transaction.api.domain.TGlobalTransactionProcessStatus
import com.isuwang.soa.transaction.utils.{DateUtils, ErrorCode}
import org.slf4j.{LoggerFactory, Logger}
import wangzx.scala_commons.sql._


/**
  * Created by tangliu on 2016/4/12.
  */
class GlobalTransactionProcessUpdateAction(processId: Int, responseJson: String, status: TGlobalTransactionProcessStatus) extends Action[Unit] {

  val LOGGER: Logger = LoggerFactory.getLogger(classOf[GlobalTransactionProcessUpdateAction])

  /**
    * 输入检查：查询、新增、更新、删除等输入条件
    */
  override def inputCheck: Unit = {
    assert(processId > 0, ErrorCode.INPUTERROR.getCode, "globalTransactionProcess.id 错误")
  }

  /**
    * 动作
    */
  override def action: Unit = {

    val processOpt = TransactionSQL.getTransactionProcessForUpdate(processId)

    val now: Date = DateUtils.resetMillisecond(new Date)
    val updatedAt = new Timestamp(now.getTime)

    val header = TransactionContext.Factory.getCurrentInstance().getHeader
    val updatedBy = if (header.getOperatorId.isPresent) header.getOperatorId.get else 0

    if (!processOpt.isDefined)
      throw new SoaException(ErrorCode.NOTEXIST.getCode, ErrorCode.NOTEXIST.getMsg)
    else {

      val process = processOpt.get

      LOGGER.info("更新试过过程({})前,状态({}),过程响应参数({})", process.id.toString, process.status.toString, process.responseJson);

      esql(
        sql"""
                update global_transaction_process
                set
                  status = ${status.getValue},
                  responseJson = ${responseJson}
                  updated_at = ${updatedAt}
                  updated_by = ${updatedBy}
                where id = ${processId}
            """
      )

      LOGGER.info("更新试过过程({})后,状态({}),过程响应参数({})", process.id.toString, status.getValue.toString, responseJson);
    }

  }

  /**
    * 后置条件检查
    */
  override def postCheck: Unit = {}

  /**
    * 前置条件检查：动作、状态等业务逻辑
    */
  override def preCheck: Unit = {}
}
