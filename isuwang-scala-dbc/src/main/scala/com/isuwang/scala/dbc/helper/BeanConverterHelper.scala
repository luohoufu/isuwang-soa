package com.isuwang.scala.dbc.helper

import com.isuwang.scala.dbc.helper.BeanMapping._
import org.apache.commons.lang.math.NumberUtils
import org.apache.thrift.TEnum
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by caiwb on 15-8-8.
  */

class BeanConverterException(cause: Throwable) extends RuntimeException(cause)

object BeanConverterHelper {
  val LOGGER: Logger = LoggerFactory.getLogger(classOf[BeanConverterException])

  def copys[T <: AnyRef](srcs: List[AnyRef], destClass: Class[T]): List[T] = srcs.map(copy(_, destClass, false)).toSeq.toList

  def copys[T <: AnyRef](srcs: List[AnyRef], destClass: Class[T], setDefaultValForNull: Boolean = false): List[T] = srcs.map(copy(_, destClass, setDefaultValForNull)).toSeq.toList

  def copy[T <: AnyRef](src: AnyRef, destClass: Class[T]): T = copy(src, destClass, false)

  /**
    * 值复制
    *
    * TODO
    *
    * @param src
    * @param destClass
    * @param setDefaultValForNull 是否为null值属性设置默认值（null=>0,null=>""）
    * @return
    * @throws BeanConverterException
    */
  @throws(classOf[BeanConverterException])
  def copy[T <: AnyRef](src: AnyRef, destClass: Class[T], setDefaultValForNull: Boolean = false): T = {

    if (src == null) return null.asInstanceOf[T]

    val dest = destClass.newInstance

    val srcBeanMapping = BeanMapping.getBeanMapping(src.getClass).asInstanceOf[BeanMapping[AnyRef]]
    val destBeanMapping = BeanMapping.getBeanMapping(destClass).asInstanceOf[BeanMapping[AnyRef]]

    for (destFieldMapping <- destBeanMapping.fields) {
      srcBeanMapping.getFieldByName(destFieldMapping.fieldName) match {
        case Some(srcFieldMapping) =>

          val srcValue = srcFieldMapping.get(src)
          val destDefault = destFieldMapping.get(dest)

          val destValue = {
            if ((srcValue == null || srcValue == None) && (destDefault != null && destDefault != None)) null
            else {
              destFieldMapping.fieldType match {
                case java.lang.Boolean.TYPE | ClassOfBoolean => if (srcValue.toString.matches("[0|1]")) "1" == srcValue.toString
                case java.lang.Byte.TYPE | ClassOfByte => if (srcValue != null) java.lang.Byte.valueOf(srcValue.toString) else if (setDefaultValForNull) java.lang.Byte.valueOf("0")
                case java.lang.Short.TYPE | ClassOfShort => if (srcValue != null) java.lang.Short.valueOf(srcValue.toString) else if (setDefaultValForNull) java.lang.Short.valueOf("0")
                case java.lang.Integer.TYPE | ClassOfInteger =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case java.lang.Integer.TYPE | ClassOfInteger => srcValue
                      case java.lang.Short.TYPE | ClassOfShort => srcValue
                      case java.lang.Boolean.TYPE | ClassOfBoolean => if (java.lang.Boolean.parseBoolean(srcValue.toString)) 1 else 0
                      case ClassOfUtilDate => srcValue.asInstanceOf[java.util.Date].getTime.toInt
                      case ClassOfSQLDate => srcValue.asInstanceOf[java.sql.Date].getTime.toInt
                      case ClassOfSQLTime => srcValue.asInstanceOf[java.sql.Time].getTime.toInt
                      case ClassOfSQLTimestamp => srcValue.asInstanceOf[java.sql.Timestamp].getTime.toInt
                      case ClassOfTEnum => srcValue.asInstanceOf[org.apache.thrift.TEnum].getValue
                      case ClassOfOption => srcValue.asInstanceOf[scala.Option[Int]].get
                      case _ => if (ClassOfTEnum.isAssignableFrom(srcFieldMapping.fieldType)) srcValue.asInstanceOf[org.apache.thrift.TEnum].getValue
                    }
                  else if (setDefaultValForNull) java.lang.Integer.valueOf("0")
                  else null
                case java.lang.Long.TYPE | ClassOfLong =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case java.lang.Long.TYPE | ClassOfLong => srcValue
                      case ClassOfUtilDate => srcValue.asInstanceOf[java.util.Date].getTime
                      case ClassOfSQLDate => srcValue.asInstanceOf[java.sql.Date].getTime
                      case ClassOfSQLTime => srcValue.asInstanceOf[java.sql.Time].getTime
                      case ClassOfSQLTimestamp => srcValue.asInstanceOf[java.sql.Timestamp].getTime
                      case _ => null
                    }
                  else if (setDefaultValForNull) java.lang.Long.valueOf("0")
                  else null
                case java.lang.Float.TYPE | ClassOfFloat => if (srcValue != null) java.lang.Float.valueOf(srcValue.toString) else if (setDefaultValForNull) java.lang.Float.valueOf("0")
                case java.lang.Double.TYPE | ClassOfDouble =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case java.lang.Double.TYPE | ClassOfDouble => srcValue
                      case ClassOfBigDecimal => srcValue.asInstanceOf[java.math.BigDecimal].doubleValue
                      case ClassOfScalaBigDecimal => srcValue.asInstanceOf[scala.math.BigDecimal].doubleValue
                      case _ => null
                    }
                  else if (setDefaultValForNull) java.lang.Double.valueOf("0")
                  else null
                case ClassOfString =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case ClassOfString => srcValue
                      case ClassOfEnum => srcValue.asInstanceOf[java.lang.Enum[_]].name
                      case _ => if (ClassOfEnum.isAssignableFrom(srcFieldMapping.fieldType)) srcValue.asInstanceOf[java.lang.Enum[_]].name else srcValue.toString
                    }
                  else if (setDefaultValForNull) new java.lang.String("")
                  else null
                case ClassOfBigDecimal =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case java.lang.Double.TYPE | ClassOfDouble => new java.math.BigDecimal(srcValue.toString)
                      case ClassOfBigDecimal => srcValue
                      case ClassOfScalaBigDecimal => new java.math.BigDecimal(srcValue.asInstanceOf[scala.math.BigDecimal].toString)
                      case _ => null
                    }
                  else if (setDefaultValForNull) new java.math.BigDecimal(0)
                  else null
                case ClassOfScalaBigDecimal =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case java.lang.Double.TYPE | ClassOfDouble => scala.math.BigDecimal(srcValue.toString)
                      case ClassOfBigDecimal => scala.math.BigDecimal(srcValue.asInstanceOf[java.math.BigDecimal].doubleValue)
                      case ClassOfScalaBigDecimal => srcValue
                      case _ => null
                    }
                  else if (setDefaultValForNull) scala.math.BigDecimal(0)
                  else null
                case ClassOfUtilDate =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case java.lang.Long.TYPE | ClassOfLong | java.lang.Integer.TYPE | ClassOfInteger if srcValue != 0 => new java.util.Date(java.lang.Long.valueOf(srcValue.toString))
                      case ClassOfUtilDate => srcValue
                      case ClassOfSQLDate => new java.util.Date(srcValue.asInstanceOf[java.sql.Date].getTime)
                      case ClassOfSQLTime => new java.util.Date(srcValue.asInstanceOf[java.sql.Time].getTime)
                      case ClassOfSQLTimestamp => new java.util.Date(srcValue.asInstanceOf[java.sql.Timestamp].getTime)
                      case _ => null
                    }
                  else if (setDefaultValForNull) new java.util.Date
                  else null
                case ClassOfSQLDate =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case java.lang.Long.TYPE | ClassOfLong | java.lang.Integer.TYPE | ClassOfInteger if srcValue != 0 => new java.sql.Date(java.lang.Long.valueOf(srcValue.toString))
                      case ClassOfUtilDate => new java.sql.Date(srcValue.asInstanceOf[java.util.Date].getTime)
                      case ClassOfSQLDate => srcValue
                      case ClassOfSQLTime => new java.sql.Date(srcValue.asInstanceOf[java.sql.Time].getTime)
                      case ClassOfSQLTimestamp => new java.sql.Date(srcValue.asInstanceOf[java.sql.Timestamp].getTime)
                      case _ => null
                    }
                  else if (setDefaultValForNull) new java.sql.Date(new java.util.Date().getTime)
                  else null
                case ClassOfSQLTime =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case java.lang.Long.TYPE | ClassOfLong | java.lang.Integer.TYPE | ClassOfInteger if srcValue != 0 => new java.sql.Time(java.lang.Long.valueOf(srcValue.toString))
                      case ClassOfUtilDate => new java.sql.Time(srcValue.asInstanceOf[java.util.Date].getTime)
                      case ClassOfSQLDate => new java.sql.Time(srcValue.asInstanceOf[java.sql.Date].getTime)
                      case ClassOfSQLTime => srcValue
                      case ClassOfSQLTimestamp => new java.sql.Time(srcValue.asInstanceOf[java.sql.Timestamp].getTime)
                      case _ => null
                    }
                  else if (setDefaultValForNull) new java.sql.Time(new java.util.Date().getTime)
                  else null
                case ClassOfSQLTimestamp =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case java.lang.Long.TYPE | ClassOfLong | java.lang.Integer.TYPE | ClassOfInteger if srcValue != 0 => new java.sql.Timestamp(java.lang.Long.valueOf(srcValue.toString))
                      case ClassOfUtilDate => new java.sql.Timestamp(srcValue.asInstanceOf[java.util.Date].getTime)
                      case ClassOfSQLDate => new java.sql.Timestamp(srcValue.asInstanceOf[java.sql.Date].getTime)
                      case ClassOfSQLTime => new java.sql.Timestamp(srcValue.asInstanceOf[java.sql.Time].getTime)
                      case ClassOfSQLTimestamp => srcValue
                      case _ => null
                    }
                  else if (setDefaultValForNull) new java.sql.Timestamp(new java.util.Date().getTime)
                  else null
                case ClassOfByteArray =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case ClassOfByteBuffer => {
                        val byteBuffer = srcValue.asInstanceOf[java.nio.ByteBuffer]
                        java.util.Arrays.copyOfRange(byteBuffer.array, byteBuffer.position, byteBuffer.limit)
                      }
                      case ClassOfByteArray => srcValue
                      case _ => null
                    }
                  else null
                case ClassOfByteBuffer =>
                  if (srcValue != null)
                    srcFieldMapping.fieldType match {
                      case ClassOfByteBuffer => srcValue
                      case ClassOfByteArray => {
                        val byteArray = srcValue.asInstanceOf[Array[Byte]]
                        java.nio.ByteBuffer.wrap(byteArray)
                      }
                      case _ => null
                    }
                  else null
                case ClassOfEnum | ClassOfTEnum => {
                  if (srcValue != null) {
                    val enumConstants = destFieldMapping.fieldType.getEnumConstants
                    for (enumConstant <- enumConstants) {
                      if (srcFieldMapping.fieldType == ClassOfString && enumConstant.asInstanceOf[java.lang.Enum[_]].name == srcValue.toString) enumConstant
                      else if (NumberUtils.isNumber(srcValue.toString) && enumConstant.isInstanceOf[TEnum] && enumConstant.asInstanceOf[TEnum].getValue == NumberUtils.toInt(srcValue.toString)) enumConstant
                      else if (NumberUtils.isNumber(srcValue.toString) && enumConstant.asInstanceOf[java.lang.Enum[_]].ordinal == NumberUtils.toInt(srcValue.toString)) enumConstant
                      else null
                    }
                  }
                }
                case ClassOfOption => {
                  Some(srcValue)
                }
                case ClassOfJavaOption => {
                  // TODO
                  if (srcValue == null)
                    java.util.Optional.empty()
                  else {
                    // srcValue -> destField
                    val destValue = (srcValue.getClass, destFieldMapping.optionalInnerType) match {
                      case (x, y) if x == y => srcValue
                      case (x, y) if x == classOf[scala.math.BigDecimal] && y == classOf[java.lang.Double] =>
                        new java.lang.Double(srcValue.asInstanceOf[scala.math.BigDecimal].doubleValue())
                      case (x, y) if x == classOf[java.sql.Timestamp] && y == classOf[java.lang.Long] =>
                        new java.lang.Long(srcValue.asInstanceOf[java.sql.Timestamp].getTime)
                      case _ =>
                        srcValue
                    }
                    java.util.Optional.of(destValue)
                  }
                }
                case _ =>
                  f(srcValue, srcFieldMapping.fieldType, destFieldMapping.fieldType)
              }
            }
          }
          try {
            if (destValue != null && destValue != None) {
              destFieldMapping.asInstanceOf[destBeanMapping.FieldMapping[Any]].set(dest, destValue)
            }
          } catch {
            case e: Throwable =>
              LOGGER.error(e.getMessage + " " + destFieldMapping.fieldName + "," + destValue)
              throw e
          }
        case None =>
      }
    }

    dest
  }

  private def f(srcValue: Any, srcFieldType: Class[_], destFieldType: Class[_]): Any = {
    if (srcValue != null && ClassOfEnum.isAssignableFrom(destFieldType)) {
      val enumConstants = destFieldType.getEnumConstants
      for (enumConstant <- enumConstants) {
        if (srcFieldType == ClassOfString) {
          if (enumConstant.asInstanceOf[java.lang.Enum[_]].name == srcValue.toString) return enumConstant
        } else if (NumberUtils.isNumber(srcValue.toString)) {
          if (enumConstant.isInstanceOf[TEnum]) {
            if (enumConstant.asInstanceOf[TEnum].getValue == NumberUtils.toInt(srcValue.toString)) return enumConstant
          } else {
            if (enumConstant.asInstanceOf[java.lang.Enum[_]].ordinal == NumberUtils.toInt(srcValue.toString)) return enumConstant
          }
        }
      }
    }

    null
  }

  //  def main(args: Array[String]) {
  //    val order = new TOrder
  //    order.status = 4
  //    order.amount = 8.88
  //    var entity: Orders = copy(order, classOf[Orders], true)
  //
  //    println(entity)
  //  }
}
