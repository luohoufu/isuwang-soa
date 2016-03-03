package com.isuwang.soa.doc.cache;

import com.isuwang.soa.core.TBeanSerializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

/**
 * Created by tangliu on 2016/3/3.
 */
public class GetServiceMetadata_argsSerializer implements TBeanSerializer<getServiceMetadata_args> {

    @Override
    public void read(getServiceMetadata_args bean, TProtocol iprot) throws TException {

        TField schemeField;
        iprot.readStructBegin();

        while (true) {
            schemeField = iprot.readFieldBegin();
            if (schemeField.type == TType.STOP) {
                break;
            }
            switch (schemeField.id) {
                default:
                    TProtocolUtil.skip(iprot, schemeField.type);

            }
            iprot.readFieldEnd();
        }
        iprot.readStructEnd();

        validate(bean);
    }


    @Override
    public void write(getServiceMetadata_args bean, TProtocol oprot) throws TException {

        validate(bean);
        oprot.writeStructBegin(new TStruct("getServiceMetadata_args"));
        oprot.writeFieldStop();
        oprot.writeStructEnd();
    }

    public void validate(getServiceMetadata_args bean) throws TException {
    }

    @Override
    public String toString(getServiceMetadata_args bean) {
        return bean == null ? "null" : bean.toString();
    }

}

