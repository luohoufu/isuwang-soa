package com.isuwang.soa.great;

import com.isuwang.soa.core.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

public class GreatServiceCodec {


    public static class sayGreat_args {

        private String msg;

        public String getMsg() {
            return this.msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

    }


    public static class sayGreat_result {

              /*
              private SoaException soaException;

              public SoaException getSoaException(){
                return soaException;
              }

              public void setSoaException(SoaException soaException){
                this.soaException = soaException;
              }
              */


    }

    public static class SayGreat_argsSerializer implements TBeanSerializer<sayGreat_args> {

        @Override
        public void read(sayGreat_args bean, TProtocol iprot) throws TException {

            TField schemeField;
            iprot.readStructBegin();

            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == TType.STOP) {
                    break;
                }

                switch (schemeField.id) {

                    case 1:
                        if (schemeField.type == TType.STRING) {
                            bean.setMsg(iprot.readString());
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

            validate(bean);
        }


        @Override
        public void write(sayGreat_args bean, TProtocol oprot) throws TException {

            validate(bean);
            oprot.writeStructBegin(new TStruct("sayGreat_args"));


            oprot.writeFieldBegin(new TField("msg", TType.STRING, (short) 1));
            oprot.writeString(bean.getMsg());
            oprot.writeFieldEnd();

            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }


        public void validate(sayGreat_args bean) throws TException {

            if (bean.getMsg() == null)
                throw new SoaException(SoaBaseCode.NotNull, "msg字段不允许为空");

        }

    }

    public static class SayGreat_resultSerializer implements TBeanSerializer<sayGreat_result> {
        @Override
        public void read(sayGreat_result bean, TProtocol iprot) throws TException {

            TField schemeField;
            iprot.readStructBegin();

            A:
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == TType.STOP) {
                    break;
                }

                switch (schemeField.id) {
                    case 0:  //SUCCESS
                        if (schemeField.type == TType.VOID) {
                            TProtocolUtil.skip(iprot, schemeField.type);
                        } else {
                            TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    /*
                    case 1: //ERROR
                        bean.setSoaException(new SoaException());
                        new SoaExceptionSerializer().read(bean.getSoaException(), iprot);
                        break A;
                    */
                    default:
                        TProtocolUtil.skip(iprot, schemeField.type);
                }
                iprot.readFieldEnd();
            }
            iprot.readStructEnd();

            validate(bean);
        }

        @Override
        public void write(sayGreat_result bean, TProtocol oprot) throws TException {

            validate(bean);
            oprot.writeStructBegin(new TStruct("sayGreat_result"));


            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }


        public void validate(sayGreat_result bean) throws TException {

        }

    }

    public static class sayGreat<I extends com.isuwang.soa.great.service.GreatService> extends SoaProcessFunction<I, sayGreat_args, sayGreat_result, SayGreat_argsSerializer, SayGreat_resultSerializer> {
        public sayGreat() {
            super("sayGreat", new SayGreat_argsSerializer(), new SayGreat_resultSerializer());
        }

        @Override
        public sayGreat_result getResult(I iface, sayGreat_args args) throws TException {
            sayGreat_result result = new sayGreat_result();

            iface.sayGreat(args.msg);

            return result;
        }

        @Override
        public sayGreat_args getEmptyArgsInstance() {
            return new sayGreat_args();
        }

        @Override
        protected boolean isOneway() {
            return false;
        }
    }


    public static class SoaExceptionSerializer implements TBeanSerializer<SoaException> {

        @Override
        public void read(SoaException bean, TProtocol iprot) throws TException {
            TField schemeField;
            iprot.readStructBegin();

            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == TType.STOP) {
                    break;
                }
                switch (schemeField.id) {
                    case 1: // code
                        if (schemeField.type == TType.STRING) {
                            bean.setCode(iprot.readString());
                        } else {
                            TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2: // msg
                        if (schemeField.type == TType.STRING) {
                            bean.setMsg(iprot.readString());
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

            // check for required fields of primitive type, which can't be checked in the validate method
            validate(bean);
        }


        @Override
        public void write(SoaException bean, TProtocol oprot) throws TException {
            validate(bean);

            oprot.writeStructBegin(new TStruct("SoaException"));
            oprot.writeFieldBegin(new TField("code", TType.STRING, (short) 1));
            oprot.writeString(bean.getCode());
            oprot.writeFieldEnd();

            oprot.writeFieldBegin(new TField("msg", TType.STRING, (short) 2));
            oprot.writeString(bean.getMsg());
            oprot.writeFieldEnd();

            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }

        @Override
        public void validate(SoaException bean) throws TException {
            if (bean.getCode() == null)
                throw new SoaException(SoaBaseCode.NotNull, "code字段不允许为空");

            if (bean.getMsg() == null)
                throw new SoaException(SoaBaseCode.NotNull, "msg字段不允许为空");
        }
    }

    public static class Processor<I extends com.isuwang.soa.great.service.GreatService> extends SoaBaseProcessor {
        public Processor(I iface) {
            super(iface, getProcessMap(new java.util.HashMap<>()));
        }

        private static <I extends com.isuwang.soa.great.service.GreatService> java.util.Map<String, SoaProcessFunction<I, ?, ?, ? extends TBeanSerializer<?>, ? extends TBeanSerializer<?>>> getProcessMap(java.util.Map<String, SoaProcessFunction<I, ?, ?, ? extends TBeanSerializer<?>, ? extends TBeanSerializer<?>>> processMap) {

            processMap.put("sayGreat", new sayGreat());

            return processMap;
        }
    }

}
      