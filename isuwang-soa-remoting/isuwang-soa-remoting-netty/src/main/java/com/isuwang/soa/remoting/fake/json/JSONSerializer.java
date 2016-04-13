package com.isuwang.soa.remoting.fake.json;//package com.isuwang.soa.remoting.fake.json;

import com.google.gson.*;
import com.isuwang.soa.core.SoaException;
import com.isuwang.soa.core.metadata.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON序列化
 *
 * @author craneding
 * @date 15/4/26
 */
public class JSONSerializer extends TBaseBeanSerializer {

    private Service service;

    public void setService(Service service) {
        this.service = service;
    }


    @Override
    public void read(InvocationInfo invocationInfo, TProtocol iprot) throws TException {
        DataInfo dataInfo = invocationInfo.getDataInfo();

        Struct response = dataInfo.getMethod().getResponse();

        JsonObject responseJSON = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        iprot.readStructBegin();

        TField schemeField;

        while (true) {

            schemeField = iprot.readFieldBegin();
            if (schemeField.type == TType.STOP)
                break;

            if (schemeField.id == 0) {

                List<Field> fields = response.getFields();
                Field field = fields.isEmpty() ? null : fields.get(0);
                if (field != null) {
                    DataType dataType = field.getDataType();
                    readField(iprot, null, dataType, jsonArray, schemeField, dataInfo.getService());
                } else {
                    TProtocolUtil.skip(iprot, schemeField.type);
                }

            } else if (schemeField.id == 1) {

                String errCode = "", errMsg = "";
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == TType.STOP) {
                        break;
                    }
                    switch (schemeField.id) {
                        case 1: // ERR_CODE
                            if (schemeField.type == TType.STRING) {
                                errCode = iprot.readString();
                            } else {
                                TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2: // ERR_MSG
                            if (schemeField.type == TType.STRING) {
                                errMsg = iprot.readString();
                            } else {
                                TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        default:
                            TProtocolUtil.skip(iprot, schemeField.type);
                    }
                    iprot.readFieldEnd();
                }
                iprot.readStructEnd();

                throw new SoaException(errCode, errMsg);

            } else {
                TProtocolUtil.skip(iprot, schemeField.type);
            }
            iprot.readFieldEnd();
        }
        iprot.readStructEnd();

        responseJSON.add("success", jsonArray.size() > 0 ? jsonArray.get(0) : null);
        responseJSON.addProperty("responseCode", "0");
        responseJSON.addProperty("responseMsg", "成功");

        invocationInfo.setResponseData(responseJSON.toString());
    }

    private JsonElement readField(TProtocol iprot, Field field, DataType dataType, JsonElement jsonElement, TField schemeField, Service service) throws TException {
        JsonElement value = null;

        switch (dataType.getKind()) {
            case VOID:
                break;
            case BOOLEAN:
                value = new JsonPrimitive(iprot.readBool());
                break;
            case BYTE:
                value = new JsonPrimitive(iprot.readByte());
                break;
            case SHORT:
                value = new JsonPrimitive(iprot.readI16());
                break;
            case INTEGER:
                value = new JsonPrimitive(iprot.readI32());
                break;
            case LONG:
                value = new JsonPrimitive(iprot.readI64());
                break;
            case DOUBLE:
                value = new JsonPrimitive(iprot.readDouble());
                break;
            case STRING:
                value = new JsonPrimitive(iprot.readString());
                break;
            case BINARY:
                break;
            case DATE:
                Long time = iprot.readI64();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date date = new java.util.Date(time);
                value = new JsonPrimitive(sdf.format(date));
                break;
            case BIGDECIMAL:
                value = new JsonPrimitive(new BigDecimal(iprot.readString()));
                break;
            case MAP:
                //if(schemeField.type == TType.MAP) {
                TMap tMap = iprot.readMapBegin();

                JsonObject jsonMap = new JsonObject();
                for (int i = 0; i < tMap.size; i++) {
                    JsonElement keyElement = readField(iprot, null, dataType.getKeyType(), null, schemeField, service);
                    JsonElement valueElement = readField(iprot, null, dataType.getValueType(), null, schemeField, service);

                    jsonMap.add(keyElement.getAsString(), valueElement);
                }

                iprot.readMapEnd();

                value = jsonMap;
                break;
            case LIST:
//                if(schemeField.type == TType.LIST) {
                TList tList = iprot.readListBegin();

                JsonArray jsonElements = new JsonArray();
                for (int i = 0; i < tList.size; i++) {
                    readField(iprot, null, dataType.getValueType(), jsonElements, null, service);
                }

                iprot.readListEnd();

                value = jsonElements;
                break;
            case SET:
//                if(schemeField.type == TType.SET) {
                TSet tSet = iprot.readSetBegin();

                JsonArray jsonElements1 = new JsonArray();
                for (int i = 0; i < tSet.size; i++) {
                    readField(iprot, null, dataType.getValueType(), jsonElements1, schemeField, service);
                }

                iprot.readSetEnd();

                value = jsonElements1;
//
                break;
            case ENUM:
                TEnum tEnum = findEnum(dataType.getQualifiedName(), service);

                String enumItemLabel = findEnumItemLabel(tEnum, iprot.readI32());

                value = new JsonPrimitive(enumItemLabel);
                break;
            case STRUCT:
                iprot.readStructBegin();

                Struct struct = findStruct(dataType.getQualifiedName(), service);

                JsonObject jsonObject = new JsonObject();

                do {
                    TField tField = iprot.readFieldBegin();

                    if (tField.type == TType.STOP)
                        break;

                    Field field1 = findField(tField.id, struct);

                    readField(iprot, field1, field1.getDataType(), jsonObject, tField, service);
                } while (true);

                iprot.readFieldEnd();
                iprot.readStructEnd();

                value = jsonObject;
                break;
        }

        if (jsonElement != null) {
            if (jsonElement.isJsonArray()) {
                ((JsonArray) jsonElement).add(value);
            } else if (jsonElement.isJsonObject() && field != null) {
                ((JsonObject) jsonElement).add(field.getName(), value);
            }
        }
        return value;
    }

    @Override
    public void write(InvocationInfo invocationInfo, TProtocol oprot) throws TException {
        DataInfo dataInfo = invocationInfo.getDataInfo();
        // consumesValue like { serviceName: , version:, methodName:, params: { arg1:, arg2, }}
        String consumesValue = dataInfo.getConsumesValue();

        JsonObject jsonObject = new JsonParser().parse(consumesValue).getAsJsonObject();

        JsonElement serviceName = jsonObject.get("serviceName");
        JsonElement version = jsonObject.get("version");
        JsonElement methodName = jsonObject.get("methodName");
        JsonElement params = jsonObject.get("params");
        JsonObject methodParamers = new JsonObject();

        if (serviceName == null)
            throw new TException("not fund service name in request.");

        if (version == null)
            throw new TException("not fund service version in request.");

        if (methodName == null)
            throw new TException("not fund method name in request.");

        if (service == null)
            throw new TException("not fund service(" + serviceName.getAsString() + "," + version.getAsString() + ") in cache.");

        dataInfo.setService(service);
        dataInfo.setMethod(findMethod(methodName.getAsString(), service));

        if (dataInfo.getMethod() == null)
            throw new TException("not fund method(" + methodName.getAsString() + ") in service.");


        oprot.writeStructBegin(new TStruct(dataInfo.getMethod().getRequest().getName()));
        if (params == null)
            throw new TException("not fund params in request.");

        Set<Map.Entry<String, JsonElement>> entries = new LinkedHashSet<>(methodParamers.entrySet());
        entries.addAll(params.getAsJsonObject().entrySet());

        // entries like { requestHeader: , arg1: arg2, }

        for (Map.Entry<String, JsonElement> entry : entries) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            Field field = findField(key, dataInfo.getMethod().getRequest());

            if (field == null)
                throw new TException("not fund " + key + " in request's method.");

            oprot.writeFieldBegin(new TField(field.getName(), dataType2Byte(field.getDataType()), (short) field.getTag()));
            writeField(service, field.getDataType(), oprot, value);
            oprot.writeFieldEnd();
        }

        oprot.writeFieldStop();
        oprot.writeStructEnd();
    }


}
