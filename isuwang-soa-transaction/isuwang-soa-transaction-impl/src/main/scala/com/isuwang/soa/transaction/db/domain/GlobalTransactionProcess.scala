package com.isuwang.soa.transaction.db.domain

import wangzx.scala_commons.sql._

/**
  *
  * @author Generated
  */
@Table(value = "global_transaction_process", camelToUnderscore = true)
class GlobalTransactionProcess extends java.io.Serializable {
  /**
    *
    */
  @Id(auto = true)
  var id: Int = _

  /**
    *
    */
  var transactionId: Int = _

  /**
    * 过程所属序列号
    */
  var transactionSequence: Int = _

  /**
    * 过程当前状态，1：新建；2：成功；3：失败；4：未知，5：已回滚；
    */
  var status: Int = _

  /**
    * 过程目标状态，1：成功；2：已回滚；
    */
  var expectedStatus: Int = _

  /**
    * 服务名称
    */
  var serviceName: String = _

  /**
    * 服务版本
    */
  var versionName: String = _

  /**
    * 方法名称
    */
  var methodName: String = _

  /**
    * 回滚方法名称
    */
  var rollbackMethodName: String = _

  /**
    * 过程请求参数Json序列化
    */
  var requestJson: String = _

  /**
    * 过程响应参数Json序列化
    */
  var responseJson: String = _

  /**
    * 重试次数
    */
  var redoTimes: Int = _

  /**
    * 下次重试时间
    */
  var nextRedoTime: java.sql.Timestamp = _

  /**
    *
    */
  var createdAt: java.sql.Timestamp = _

  /**
    *
    */
  var updatedAt: java.sql.Timestamp = _

  /**
    *
    */
  var createdBy: Int = _

  /**
    *
    */
  var updatedBy: Int = _


}
