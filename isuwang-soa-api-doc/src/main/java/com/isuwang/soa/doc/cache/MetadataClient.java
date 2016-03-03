package com.isuwang.soa.doc.cache;

import com.isuwang.soa.remoting.BaseServiceClient;
import org.apache.thrift.TException;

/**
 * Created by tangliu on 2016/3/3.
 */
public class MetadataClient extends BaseServiceClient {

    public MetadataClient(String serviceName, String versionName) {
        super(serviceName, versionName);
    }

    /**
     * getServiceMetadata
     **/
    public String getServiceMetadata() throws TException {

        initContext("getServiceMetadata");
        try {
            getServiceMetadata_args getServiceMetadata_args = new getServiceMetadata_args();
            getServiceMetadata_result response = sendBase(getServiceMetadata_args, new getServiceMetadata_result(), new GetServiceMetadata_argsSerializer(), new GetServiceMetadata_resultSerializer());
            return response.getSuccess();
        } finally {
            destoryContext();
        }
    }


}
