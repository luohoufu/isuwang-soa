package com.isuwang.soa.transaction

import java.sql.ResultSet

import TransactionDB._
import com.isuwang.soa.transaction.db.domain.{GlobalTransaction, GlobalTransactionProcess}
import wangzx.scala_commons.sql._

/**
  * Created by tangliu on 2016/4/12.
  */
object TransactionSQL {

  def getTransactionProcessForUpdate(id: Int): Option[GlobalTransactionProcess] = {
    row[GlobalTransactionProcess](sql"select * from global_transaction_process where id = ${id} for update");
  }

  def getTransactionForUpdate(id: Int): Option[GlobalTransaction] = {
    row[GlobalTransaction](sql"select * from global_transactions where id = ${id} for update");
  }

  def insertTransactionProcess(E: GlobalTransactionProcess): Int = {
    var id = 0

    val sqlInsert =
      sql"""
         insert into global_transaction_process
         set
            transaction_id = ${E.transactionId},
            transaction_sequence = ${E.transactionSequence},
            status = ${E.status},
            expected_status = ${E.expectedStatus},
            service_name = ${E.serviceName},
            version_name = ${E.versionName},
            method_name = ${E.methodName},
            rollback_method_name = ${E.rollbackMethodName},
            requestJson = ${E.requestJson},
            responseJson = ${E.responseJson},
            redo_times = ${E.redoTimes},
            next_redo_time = ${E.nextRedoTime},
            created_at = ${E.createdAt},
            updated_at = ${E.updatedAt},
            created_by = ${E.createdBy},
            updated_by = ${E.updatedBy},
        """
    esqlWithGenerateKey(sqlInsert) { rs: ResultSet =>
      if (rs.next()) id = rs.getInt(1)
    }

    id
  }


  def insertTransaction(E: GlobalTransaction): Int = {
    var id = 0

    val sqlInsert =
      sql"""
         insert into global_transactions
         set
            status = ${E.status},
            curr_sequence = ${E.currSequence},
            created_at = ${E.createdAt},
            updated_at = ${E.updatedAt},
            created_by = ${E.createdBy},
            updated_by = ${E.updatedBy},
        """
    esqlWithGenerateKey(sqlInsert) { rs: ResultSet =>
      if (rs.next()) id = rs.getInt(1)
    }
    id
  }

}
