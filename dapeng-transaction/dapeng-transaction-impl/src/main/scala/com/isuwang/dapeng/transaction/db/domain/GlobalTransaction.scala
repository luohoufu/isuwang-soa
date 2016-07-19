package com.isuwang.dapeng.transaction.db.domain

import wangzx.scala_commons.sql._

/**
  *
  * @author Generated
  */
@Table(value = "global_transactions", camelToUnderscore = true)
class GlobalTransaction extends java.io.Serializable {
  /**
    *
    */
  @Id(auto = true)
  var id: Int = _

  /**
    * 状态，1：新建；2：成功；3：失败；4：已回滚；5：已部分回滚；99：挂起；
    */
  var status: Int = _

  /**
    * 当前过程序列号
    */
  var currSequence: Int = _

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
