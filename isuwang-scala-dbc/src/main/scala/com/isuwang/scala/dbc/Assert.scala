package com.isuwang.scala.dbc

import com.isuwang.soa.core.{SoaBaseCode, SoaException}

object Assert {

  def assert(assertion: scala.Boolean, code: String, message: String): scala.Unit =
    if (!assertion) throw new SoaException(code, message)

  def assert(assertion: scala.Boolean, message: String): scala.Unit =
    if (!assertion) throw new SoaException("assert", message)

  def assert(assertion: scala.Boolean): scala.Unit =
    if (!assertion) throw new SoaException("assert", "assertException")

  def assert(assertion: scala.Boolean, errorCodeEnums: SoaBaseCode): Unit = if (!assertion) throw new SoaException(errorCodeEnums.getCode, errorCodeEnums.getMsg)

  def assert(assertion: scala.Boolean, errorCodeEnums: SoaBaseCode, message: String): Unit = if (!assertion) throw new SoaException(errorCodeEnums.getCode, message)
}
