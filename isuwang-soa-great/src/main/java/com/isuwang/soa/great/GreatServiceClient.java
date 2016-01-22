package com.isuwang.soa.great;

import com.isuwang.soa.great.GreatServiceCodec.SayGreat_argsSerializer;
import com.isuwang.soa.great.GreatServiceCodec.SayGreat_resultSerializer;
import com.isuwang.soa.great.GreatServiceCodec.sayGreat_args;
import com.isuwang.soa.great.GreatServiceCodec.sayGreat_result;
import com.isuwang.soa.rpc.BaseServiceClient;
import org.apache.thrift.TException;

public class GreatServiceClient extends BaseServiceClient {

    public GreatServiceClient() {
        super("GreatService", "1.0.0");
    }


    /**
     *
     **/
    public void sayGreat(String msg) throws TException {
        initContext("sayGreat");

        try {
            sayGreat_args sayGreat_args = new sayGreat_args();
            sayGreat_args.setMsg(msg);


            sayGreat_result response = sendBase(sayGreat_args, new sayGreat_result(), new SayGreat_argsSerializer(), new SayGreat_resultSerializer());
        } finally {
            destoryContext();
        }
    }

}
      