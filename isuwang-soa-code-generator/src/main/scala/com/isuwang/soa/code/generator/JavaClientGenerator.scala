package com.isuwang.soa.code.generator

import java.util

import com.isuwang.soa.core.metadata.DataType.KIND
import com.isuwang.soa.core.metadata._

import scala.xml.Elem

/**
 * JAVA生成器
 *
 * @author tangliu
 * @date 15/9/8
 */
class JavaClientGenerator extends CodeGenerator {

  override def generate(services: util.List[Service], outDir: String): Unit = {

  }

  def toCodecTemplate(service:Service, namespaces:util.Set[String]): Elem = {
    //val structNameCache = new util.ArrayList[String]()

    return {
      <div>package {service.namespace.substring(0, service.namespace.lastIndexOf("."))};

        import com.isuwang.soa.core.*;
        import org.apache.thrift.*;
        import org.apache.thrift.protocol.*;

        import java.io.BufferedReader;
        import java.io.InputStreamReader;

        import java.util.Optional;

        public class {service.name}Codec <block>
        {
        toStructArrayBuffer(service.structDefinitions).map{(struct:Struct)=>{
          <div>public static class {struct.name}Serializer implements TBeanSerializer{lt}{struct.getNamespace() + "." + struct.name}{gt}<block>
            {getReadMethod(struct)}{getWriteMethod(struct, service)}{getValidateMethod(struct)}
            @Override
            public String toString({struct.getNamespace() + "." + struct.name} bean) <block> return bean == null ? "null" : bean.toString(); </block>
          </block>
          </div>
        }}
        }

        {
        toMethodArrayBuffer(service.methods).map{(method: Method)=>
        {
          <div>
            public static class {method.name}_args <block>
            {toFieldArrayBuffer(method.request.getFields).map{(field: Field)=>{
             <div>
               private {toDataTypeTemplate(field.getDataType)} {field.getName};

               public {toDataTypeTemplate(field.getDataType)} get{field.name.charAt(0).toUpper + field.name.substring(1)}()<block>
                  return this.{field.name};
               </block>
               public void set{field.name.charAt(0).toUpper + field.name.substring(1)}({toDataTypeTemplate(field.getDataType)} {field.name})<block>
                  this.{field.name} = {field.name};
               </block>
             </div>
            }
            }
            }

            @Override
            public String toString()<block>
              StringBuilder stringBuilder = new StringBuilder("<block>");
                {toFieldArrayBuffer(method.request.getFields).map{(field : Field) =>{

                  getToStringElement(field);
                }
                }
                }
              if(stringBuilder.lastIndexOf(",") > 0)
                stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
              stringBuilder.append("</block>");

              return stringBuilder.toString();
            </block>

          </block>


            public static class {method.name}_result <block>

            {toFieldArrayBuffer(method.response.getFields()).map{(field:Field)=>
              if(field.getDataType().getKind() == DataType.KIND.VOID) {
                <div></div>
              } else {
                <div>
                  private {toDataTypeTemplate(method.response.getFields.get(0).getDataType)} success;
                  public {toDataTypeTemplate(method.response.getFields.get(0).getDataType)} getSuccess()<block>
                    return success;
                  </block>

                  public void setSuccess({toDataTypeTemplate(method.response.getFields.get(0).getDataType)} success)<block>
                    this.success = success;
                  </block>


                  @Override
                  public String toString()<block>
                  StringBuilder stringBuilder = new StringBuilder("<block>");
                    {toFieldArrayBuffer(method.response.getFields).map{(field : Field) =>{
                      getToStringElement(field);
                    }}}
                    stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
                    stringBuilder.append("</block>");

                  return stringBuilder.toString();
                </block>

                </div>
              }
            }}
            </block>

            public static class {method.name.charAt(0).toUpper + method.name.substring(1)}_argsSerializer implements TBeanSerializer{lt}{method.name}_args{gt}<block>
            {getReadMethod(method.getRequest)}
            {getWriteMethod(method.getRequest, service)}
            {getValidateMethod(method.getRequest)}

            @Override
            public String toString({method.name}_args bean) <block> return bean == null ? "null" : bean.toString(); </block>

            </block>

            public static class {method.name.charAt(0).toUpper + method.name.substring(1)}_resultSerializer implements TBeanSerializer{lt}{method.name}_result{gt}<block>
              @Override
              public void read({method.response.name} bean, TProtocol iprot) throws TException<block>

                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();

                while(true)<block>
                  schemeField = iprot.readFieldBegin();
                  if(schemeField.type == org.apache.thrift.protocol.TType.STOP)<block> break;</block>

                  switch(schemeField.id)<block>
                    case 0:  //SUCCESS
                       if(schemeField.type == {toTDateType(method.response.fields.get(0).dataType)})<block>
                       {toReadTypeTemp(method.response.fields.get(0))}
                       </block>else<block>
                          org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        </block>
                        break;
                    /*
                    case 1: //ERROR
                        bean.setSoaException(new SoaException());
                        new SoaExceptionSerializer().read(bean.getSoaException(), iprot);
                        break A;
                    */
                    default:
                      org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                  </block>
                  iprot.readFieldEnd();
                </block>
                iprot.readStructEnd();

                validate(bean);
              </block>
            {getWriteMethod(method.getResponse, service)}
            {getValidateMethod(method.getResponse)}

            @Override
            public String toString({method.name}_result bean) <block> return bean == null ? "null" : bean.toString(); </block>
          </block>

            public static class {method.name}{lt}I extends {service.getNamespace + "." + service.name}{gt} extends SoaProcessFunction{lt}I, {method.name}_args, {method.name}_result, {method.name.charAt(0).toUpper + method.name.substring(1)}_argsSerializer,  {method.name.charAt(0).toUpper + method.name.substring(1)}_resultSerializer{gt}<block>
               public {method.name}()<block>
                   super("{method.name}", new {method.name.charAt(0).toUpper + method.name.substring(1)}_argsSerializer(),  new {method.name.charAt(0).toUpper + method.name.substring(1)}_resultSerializer());
               </block>

               @Override
               public {method.name}_result getResult(I iface, {method.name}_args args) throws TException<block>
                   {method.name}_result result = new {method.name}_result();
              {toFieldArrayBuffer(method.getResponse().getFields()).map{(field:Field)=>
                  if(field.getDataType().getKind() == DataType.KIND.VOID) {
                    <div>
                      iface.{method.name}({toFieldArrayBuffer(method.getRequest.getFields).map{ (field: Field) =>{<div>args.{field.name}{if(field != method.getRequest.fields.get(method.getRequest.fields.size() - 1)) <span>,</span>}</div>}}});
                    </div>
                  } else {
                    <div>
                      result.success = iface.{method.name}({toFieldArrayBuffer(method.getRequest.getFields).map{ (field: Field) =>{<div>args.{field.name}{if(field != method.getRequest.fields.get(method.getRequest.fields.size() - 1)) <span>,</span>}</div>}}});
                    </div>
                  }
              }}
                   return result;
               </block>

               @Override
               public {method.name}_args getEmptyArgsInstance()<block>
                  return new {method.name}_args();
               </block>

               @Override
               protected boolean isOneway()<block>
                  return false;
               </block>
            </block>
          </div>
        }
        }
        }

        public static class getServiceMetadata_args <block>

          @Override
          public String toString() <block>
            return "<block></block>";
          </block>
        </block>


        public static class getServiceMetadata_result <block>

          private String success;

          public String getSuccess() <block>
            return success;
          </block>

          public void setSuccess(String success) <block>
            this.success = success;
          </block>

          @Override
          public String toString() <block>
            StringBuilder stringBuilder = new StringBuilder("<block>");
              stringBuilder.append("\"").append("success").append("\":\"").append(this.success).append("\",");
              stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
              stringBuilder.append("</block>");

            return stringBuilder.toString();
          </block>
        </block>

        public static class GetServiceMetadata_argsSerializer implements TBeanSerializer{lt}getServiceMetadata_args{gt} <block>

          @Override
          public void read(getServiceMetadata_args bean, TProtocol iprot) throws TException <block>

            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();

            while (true) <block>
              schemeField = iprot.readFieldBegin();
              if (schemeField.type == org.apache.thrift.protocol.TType.STOP) <block>
                break;
              </block>
              switch (schemeField.id) <block>
                default:
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);

              </block>
              iprot.readFieldEnd();
            </block>
            iprot.readStructEnd();

            validate(bean);
          </block>


          @Override
          public void write(getServiceMetadata_args bean, TProtocol oprot) throws TException <block>

            validate(bean);
            oprot.writeStructBegin(new org.apache.thrift.protocol.TStruct("getServiceMetadata_args"));
            oprot.writeFieldStop();
            oprot.writeStructEnd();
          </block>

          public void validate(getServiceMetadata_args bean) throws TException <block></block>

          @Override
          public String toString(getServiceMetadata_args bean) <block>
            return bean == null ? "null" : bean.toString();
          </block>

        </block>

        public static class GetServiceMetadata_resultSerializer implements TBeanSerializer{lt}getServiceMetadata_result{gt} <block>
          @Override
          public void read(getServiceMetadata_result bean, TProtocol iprot) throws TException <block>

            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();

            while (true) <block>
              schemeField = iprot.readFieldBegin();
              if (schemeField.type == org.apache.thrift.protocol.TType.STOP) <block>
                break;
              </block>

              switch (schemeField.id) <block>
                case 0:  //SUCCESS
                if (schemeField.type == org.apache.thrift.protocol.TType.STRING) <block>
                  bean.setSuccess(iprot.readString());
                </block> else <block>
                  org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                </block>
                break;
                default:
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              </block>
              iprot.readFieldEnd();
            </block>
            iprot.readStructEnd();

            validate(bean);
          </block>

          @Override
          public void write(getServiceMetadata_result bean, TProtocol oprot) throws TException <block>

            validate(bean);
            oprot.writeStructBegin(new org.apache.thrift.protocol.TStruct("getServiceMetadata_result"));

            oprot.writeFieldBegin(new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.STRING, (short) 0));
            oprot.writeString(bean.getSuccess());
            oprot.writeFieldEnd();

            oprot.writeFieldStop();
            oprot.writeStructEnd();
          </block>

          public void validate(getServiceMetadata_result bean) throws TException <block>

            if (bean.getSuccess() == null)
            throw new SoaException(SoaBaseCode.NotNull, "success字段不允许为空");
          </block>

          @Override
          public String toString(getServiceMetadata_result bean) <block>
            return bean == null ? "null" : bean.toString();
          </block>
        </block>

        public static class getServiceMetadata{lt}I extends {service.namespace}.{service.name}{gt} extends SoaProcessFunction{lt}I, getServiceMetadata_args, getServiceMetadata_result, GetServiceMetadata_argsSerializer, GetServiceMetadata_resultSerializer{gt} <block>
          public getServiceMetadata() <block>
            super("getServiceMetadata", new GetServiceMetadata_argsSerializer(), new GetServiceMetadata_resultSerializer());
          </block>

          @Override
          public getServiceMetadata_result getResult(I iface, getServiceMetadata_args args) throws TException <block>
            getServiceMetadata_result result = new getServiceMetadata_result();

            try (InputStreamReader isr = new InputStreamReader({service.name}Codec.class.getClassLoader().getResourceAsStream("{service.namespace}.{service.name}.xml"));
            BufferedReader in = new BufferedReader(isr)) <block>
              int len = 0;
              StringBuilder str = new StringBuilder("");
              String line;
              while ((line = in.readLine()) != null) <block>

                if (len != 0) <block>
                  str.append("\r\n").append(line);
                </block> else <block>
                  str.append(line);
                </block>
                len++;
              </block>
              result.success = str.toString();

            </block> catch (Exception e) <block>
              e.printStackTrace();
              result.success = "";
            </block>

            return result;
          </block>

          @Override
          public getServiceMetadata_args getEmptyArgsInstance() <block>
            return new getServiceMetadata_args();
          </block>

          @Override
          protected boolean isOneway() <block>
            return false;
          </block>
        </block>

        @SuppressWarnings("unchecked")
        public static class Processor{lt}I extends {service.getNamespace + "." + service.name}{gt} extends SoaBaseProcessor<block>
          public Processor(I iface)<block>
            super(iface, getProcessMap(new java.util.HashMap{lt}{gt}()));
          </block>

          @SuppressWarnings("unchecked")
          private static {lt}I extends {service.getNamespace + "." + service.name}{gt} java.util.Map{lt}String, SoaProcessFunction{lt}I, ?, ?, ? extends TBeanSerializer{lt}?{gt}, ? extends TBeanSerializer{lt}?{gt}{gt}{gt} getProcessMap(java.util.Map{lt}String, SoaProcessFunction{lt}I, ?, ?, ? extends TBeanSerializer{lt}?{gt}, ? extends TBeanSerializer{lt}?{gt}{gt}{gt} processMap)<block>
            {
            toMethodArrayBuffer(service.getMethods).map{(method: Method)=>{
              <div>
                processMap.put("{method.name}", new {method.name}());
              </div>
            }
            }
            }
            processMap.put("getServiceMetadata", new getServiceMetadata());

            return processMap;
          </block>
        </block>

      </block>
      </div>
    }
  }

  def toStructName(struct: Struct): String = {
    if (struct.getNamespace == null) {
      return struct.getName()
    } else {
      return struct.getNamespace + "." + struct.getName();
    }
  }

  def toDataTypeTemplate(dataType:DataType): Elem = {
    dataType.kind match {
      case KIND.VOID => <div>void</div>
      case KIND.BOOLEAN => <div>Boolean</div>
      case KIND.BYTE => <div>Byte</div>
      case KIND.SHORT => <div>Short</div>
      case KIND.INTEGER => <div>Integer</div>
      case KIND.LONG => <div>Long</div>
      case KIND.DOUBLE => <div>Double</div>
      case KIND.STRING => <div>String</div>
      case KIND.BINARY => <div>java.nio.ByteBuffer</div>
      case KIND.DATE => <div>java.util.Date</div>
      case KIND.MAP =>
        return {<div>java.util.Map{lt}{toDataTypeTemplate(dataType.getKeyType())}, {toDataTypeTemplate(dataType.getValueType())}{gt}</div>}
      case KIND.LIST =>
        return {<div>java.util.List{lt}{toDataTypeTemplate(dataType.getValueType())}{gt}</div>}
      case KIND.SET =>
        return {<div>java.util.Set{lt}{toDataTypeTemplate(dataType.getValueType())}{gt}</div>}
      case KIND.ENUM =>
        val ref = dataType.getQualifiedName();
        return {<div>{ref}</div>}
      case KIND.STRUCT =>
        val ref = dataType.getQualifiedName();
        return {<div>{ref}</div>}
    }
  }


  def toTDateType(dataType:DataType): Elem = {
    dataType.kind match {
      case KIND.VOID => <div>org.apache.thrift.protocol.TType.VOID</div>
      case KIND.BOOLEAN => <div>org.apache.thrift.protocol.TType.BOOL</div>
      case KIND.BYTE => <div>org.apache.thrift.protocol.TType.BYTE</div>
      case KIND.SHORT => <div>org.apache.thrift.protocol.TType.I16</div>
      case KIND.INTEGER => <div>org.apache.thrift.protocol.TType.I32</div>
      case KIND.LONG => <div>org.apache.thrift.protocol.TType.I64</div>
      case KIND.DOUBLE => <div>org.apache.thrift.protocol.TType.DOUBLE</div>
      case KIND.STRING => <div>org.apache.thrift.protocol.TType.STRING</div>
      case KIND.MAP => <div>org.apache.thrift.protocol.TType.MAP</div>
      case KIND.LIST => <div>org.apache.thrift.protocol.TType.LIST</div>
      case KIND.SET => <div>org.apache.thrift.protocol.TType.SET</div>
      case KIND.ENUM => <div>org.apache.thrift.protocol.TType.I32</div>
      case KIND.STRUCT => <div>org.apache.thrift.protocol.TType.STRUCT</div>
      case KIND.DATE => <div>org.apache.thrift.protocol.TType.I64</div>
      case _ => <div></div>
    }
  }


  def toWriteTypeTemp(field: Field): Elem = {
    field.dataType.kind match {

      case KIND.BOOLEAN => <div>oprot.writeBool(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>});</div>
      case KIND.BYTE => <div>oprot.writeByte(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>});</div>
      case KIND.SHORT => <div>oprot.writeI16(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>});</div>
      case KIND.INTEGER => <div>oprot.writeI32(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>});</div>
      case KIND.LONG => <div>oprot.writeI64(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>});</div>
      case KIND.DOUBLE => <div>oprot.writeDouble(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>});</div>
      case KIND.STRING => <div>oprot.writeString(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>});</div>
      case KIND.ENUM => <div>oprot.writeI32(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>}.getValue());</div>
      case KIND.DATE =>
        return {
          <div>
            java.util.Date {field.name} = bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>};
            oprot.writeI64({field.name}.getTime());
          </div>
        }

      case KIND.LIST => return{
        <div>oprot.writeListBegin(new org.apache.thrift.protocol.TList({toTDateType(field.dataType.valueType)}, bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>}.size()));
              for({toDataTypeTemplate(field.dataType.valueType)} item : bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>})<block>
          {
            field.dataType.valueType.kind match{
              case KIND.STRUCT => <div>     new {field.dataType.valueType.qualifiedName.substring(field.dataType.valueType.qualifiedName.lastIndexOf(".")+1)}Serializer().write(item, oprot);</div>
              case KIND.STRING => <div>oprot.writeString(item);</div>
              case KIND.INTEGER => <div>oprot.writeI32(item);</div>
              case KIND.DOUBLE => <div>oprot.writeDouble(item);</div>
              case KIND.BOOLEAN => <div>oprot.writeBool(item);</div>
              case KIND.BYTE => <div>oprot.writeByte(item);</div>
              case KIND.SHORT => <div>oprot.writeI16(item);</div>
              case KIND.LONG => <div>oprot.writeI64(item);</div>
              case KIND.ENUM => <div>oprot.writeI32(item.getValue());</div>
              case KIND.DATE => <div>oprot.writeI64(item.getTime());</div>
              case _ => <div></div>
            }
          }
              </block>
              oprot.writeListEnd();
        </div>}
      case KIND.MAP => return {
        <div>oprot.writeMapBegin(new org.apache.thrift.protocol.TMap({toTDateType(field.dataType.keyType)}, {toTDateType((field.dataType.valueType))}, bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>}.size()));
             for(java.util.Map.Entry{lt}{toDataTypeTemplate(field.dataType.keyType)}, {toDataTypeTemplate(field.dataType.valueType)}{gt} item : bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>}.entrySet())<block>
          {
             field.dataType.keyType.kind match{
              case KIND.INTEGER => <div>oprot.writeI32(item.getKey());</div>
              case KIND.DOUBLE => <div>oprot.writeDouble(item.getKey());</div>
              case KIND.STRING => <div>oprot.writeString(item.getKey());</div>
              case KIND.BYTE => <div>oprot.writeByte(item.getKey());</div>
              case KIND.SHORT => <div>oprot.writeI16(item.getKey());</div>
              case KIND.LONG => <div>oprot.writeI64(item.getKey());</div>
              case KIND.ENUM => <div>oprot.writeI32(item.getKey().getValue());</div>
              case _ => <div></div>
             }
          }
          {
             field.dataType.valueType.kind match{
               case KIND.INTEGER => <div>oprot.writeI32(item.getValue());</div>
               case KIND.DOUBLE => <div>oprot.writeDouble(item.getValue());</div>
               case KIND.STRING => <div>oprot.writeString(item.getValue());</div>
               case KIND.BOOLEAN => <div>oprot.writeBool(item.getValue());</div>
               case KIND.BYTE => <div>oprot.writeByte(item.getValue());</div>
               case KIND.SHORT => <div>oprot.writeI16(item.getValue());</div>
               case KIND.LONG => <div>oprot.writeI64(item.getValue());</div>
               case KIND.ENUM => <div>oprot.writeI32(item.getValue().getValue());</div>
               case KIND.STRUCT => <div>     new {field.dataType.valueType.qualifiedName.substring(field.dataType.valueType.qualifiedName.lastIndexOf(".")+1)}Serializer().write(item.getValue(), oprot);</div>
               case _ => <div></div>
             }
          }
             </block>
          oprot.writeMapEnd();
        </div>
      }

      case KIND.STRUCT =>
        return {
          <div>  new {field.dataType.qualifiedName.substring(field.dataType.qualifiedName.lastIndexOf(".")+1)}Serializer().write(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.isOptional) <div>.get()</div>}, oprot);</div>
        }

      case KIND.VOID =>
        return {<div></div>}

      case _ => return {<div></div>}
    }

  }

  def toReadTypeTemp(field: Field): Elem = {
    field.dataType.kind match {
      case KIND.BOOLEAN => <div>   bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}iprot.readBool(){if(field.optional) <div>)</div>});</div>
      case KIND.BYTE => <div>   bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}iprot.readByte(){if(field.optional) <div>)</div>});</div>
      case KIND.SHORT => <div>   bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}iprot.readI16(){if(field.optional) <div>)</div>});</div>
      case KIND.INTEGER => <div>   bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}iprot.readI32(){if(field.optional) <div>)</div>});</div>
      case KIND.LONG => <div>   bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}iprot.readI64(){if(field.optional) <div>)</div>});</div>
      case KIND.DOUBLE => <div>   bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}iprot.readDouble(){if(field.optional) <div>)</div>});</div>
      case KIND.STRING => <div>   bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}iprot.readString(){if(field.optional) <div>)</div>});</div>
      case KIND.ENUM =>  <div>   bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}{field.dataType.qualifiedName}.findByValue(iprot.readI32()){if(field.optional) <div>)</div>});</div>
      case KIND.DATE =>
        return{
          <div>
            Long time = iprot.readI64();
            java.util.Date date = new java.util.Date(time);
            bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}date{if(field.optional) <div>)</div>});
          </div>
        }
      case KIND.LIST =>
        return {
          <div><block>
            org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
            bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}new java.util.ArrayList{lt}{gt}(_list0.size){if(field.optional) <div>)</div>});
            for(int _i2 = 0; _i2 {lt} _list0.size; ++ _i2)<block>
              {
              field.dataType.valueType.kind match {
                case KIND.BOOLEAN => <div>boolean _elem1 = iprot.readBool();</div>
                case KIND.BYTE => <div>byte _elem1 = iprot.readByte();</div>
                case KIND.SHORT => <div>short _elem1 = iprot.readI16();</div>
                case KIND.LONG => <div>long _elem1 = iprot.readI64();</div>
                case KIND.STRING => <div> String _elem1 = iprot.readString();</div>
                case KIND.INTEGER => <div> int _elem1 = iprot.readI32();</div>
                case KIND.DOUBLE => <div> double _elem1 = iprot.readDouble();</div>
                case KIND.ENUM => <div>{field.dataType.valueType.qualifiedName} _elem1 = {field.dataType.valueType.qualifiedName}.findByValue(iprot.readI32());</div>
                case KIND.STRUCT => <div>{field.dataType.valueType.qualifiedName} _elem1 = new {field.dataType.valueType.qualifiedName}();
                  new {field.dataType.valueType.qualifiedName.substring(field.dataType.valueType.qualifiedName.lastIndexOf(".")+1)}Serializer().read(_elem1, iprot);</div>
                case KIND.DATE => <div>Long time = iprot.readI64(); java.util.Date _elem1 = new java.util.Date(time);</div>
                case _ => <div></div>
              }
              }
              bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.optional) <div>.get()</div>}.add(_elem1);
            </block>
            iprot.readListEnd();
            </block>
          </div>
        }
      case KIND.MAP =>
        return {
          <div>
            org.apache.thrift.protocol.TMap _map3 = iprot.readMapBegin();
            bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}new java.util.HashMap{lt}{gt}(2 * _map3.size){if(field.optional) <div>)</div>});
            for(int _i6 = 0; _i6 {lt} _map3.size; ++ _i6)<block>
            {if(field.dataType.keyType.kind.equals(KIND.STRING)) <div>  String _key4 = iprot.readString();</div>}
            {
            field.dataType.valueType.kind match{
              case KIND.BOOLEAN => <div>boolean _val5 = iprot.readBool();</div>
              case KIND.BYTE => <div>byte _val5 = iprot.readByte();</div>
              case KIND.SHORT => <div>short _val5 = iprot.readI16();</div>
              case KIND.LONG => <div>long _val5 = iprot.readI64();</div>
              case KIND.STRING => <div>  String _val5 = iprot.readString();</div>
              case KIND.INTEGER => <div> int _val5 = iprot.readI32(); </div>
              case KIND.DOUBLE => <div> double _val5 = iprot.readDouble();</div>
              case KIND.ENUM => <div>{field.dataType.valueType.qualifiedName} _val5 = {field.dataType.valueType.qualifiedName}.findByValue(iprot.readI32());</div>
              case KIND.STRUCT => <div>{field.dataType.valueType.qualifiedName} _val5 = new {field.dataType.valueType.qualifiedName}();
                new {field.dataType.valueType.qualifiedName.substring(field.dataType.valueType.qualifiedName.lastIndexOf(".")+1)}Serializer().read(_val5, iprot);</div>
              case _ => <div></div>
            }
            }
            bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.optional) <div>.get()</div>}.put(_key4, _val5);
          </block>
            iprot.readMapEnd();
          </div>
        }
      case KIND.STRUCT =>
        return {
          <div>
            bean.set{field.name.charAt(0).toUpper + field.name.substring(1)}({if(field.optional) <div>Optional.of(</div>}new {field.dataType.qualifiedName}(){if(field.optional) <div>)</div>});
            new {field.dataType.qualifiedName.substring(field.dataType.qualifiedName.lastIndexOf(".")+1)}Serializer().read(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}(){if(field.optional) <div>.get()</div>}, iprot);
          </div>
        }
      case KIND.VOID =>
        return {<div>org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);</div>}
      case _ => <div></div>
    }
  }


  def getReadMethod(struct: Struct): Elem = {
    <div>
      @Override
      public void read({toStructName(struct)} bean, TProtocol iprot) throws TException<block>

        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();

        while(true)<block>
          schemeField = iprot.readFieldBegin();
          if(schemeField.type == org.apache.thrift.protocol.TType.STOP)<block> break;</block>

          switch(schemeField.id)<block>
          {
            toFieldArrayBuffer(struct.getFields).map{(structField : Field) =>{
              <div>
              case {structField.tag}:
                if(schemeField.type == {toTDateType(structField.dataType)})<block>
                    {toReadTypeTemp(structField)}
                </block>else<block>
                     org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                </block>
                break;
              </div>
            }}
          }
            <div>
                default:
                  org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            </div>
          </block>
          iprot.readFieldEnd();
        </block>
        iprot.readStructEnd();

        validate(bean);
      </block>
    </div>
  }


  def getWriteMethod(struct: Struct, service: Service): Elem = {
    <div>
      @Override
      public void write({toStructName(struct)} bean, TProtocol oprot) throws TException<block>

      validate(bean);
      oprot.writeStructBegin(new org.apache.thrift.protocol.TStruct("{struct.name}"));

      {toFieldArrayBuffer(struct.fields).map{(field : Field) =>{
        if(field.dataType.getKind() == DataType.KIND.VOID) {
        } else {
          if(field.isOptional){
            <div>if(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}().isPresent())<block>
              oprot.writeFieldBegin(new org.apache.thrift.protocol.TField("{field.name}", {toTDateType(field.dataType)}, (short) {field.tag}));
              {toWriteTypeTemp(field)}
              oprot.writeFieldEnd();
            </block>
            </div>
          }else{<div>
            oprot.writeFieldBegin(new org.apache.thrift.protocol.TField("{field.name}", {toTDateType(field.dataType)}, (short) {field.tag}));
            {toWriteTypeTemp(field)}
            oprot.writeFieldEnd();
          </div>
          }
        }
      }
      }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    </block>
    </div>
  }


  def getValidateMethod(struct: Struct) : Elem = {
    <div>
    public void validate({toStructName(struct)} bean) throws TException<block>
      {
      toFieldArrayBuffer(struct.fields).map{(field : Field) =>{
        <div>{
          if(!field.isOptional && field.dataType.kind != DataType.KIND.VOID){
            <div>
              if(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}() == null)
              throw new SoaException(SoaBaseCode.NotNull, "{field.name}字段不允许为空");
            </div>}}</div>
          <div>{
            if(field.dataType.kind == KIND.STRUCT && field.dataType.kind != DataType.KIND.VOID){
              <div>
                if(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}() != null)
                new {field.dataType.qualifiedName.substring(field.dataType.qualifiedName.lastIndexOf(".")+1)}Serializer().validate(bean.get{field.name.charAt(0).toUpper + field.name.substring(1)}());
              </div>}}</div>
      }
      }
      }
    </block>
    </div>
  }

  def getToStringElement(field: Field): Elem = {
    <div>
      stringBuilder.append("\"").append("{field.name}").append("\":{if(field.dataType.kind == DataType.KIND.STRING) <div>\"</div>}").append( this.{getToStringByDataType(field)}).append("{if(field.dataType.kind == DataType.KIND.STRING) <div>\"</div>},");
    </div>
  }

  def getToStringByDataType(field: Field):Elem = {
    if(field.dataType.kind == KIND.STRUCT) <div>{field.name}.toString()</div> else <div>{field.name}</div>
  }

}
