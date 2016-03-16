package com.isuwang.soa.monitor.api;

import com.isuwang.soa.monitor.api.MonitorServiceCodec.*;
import com.isuwang.soa.remoting.BaseServiceClient;
import org.apache.thrift.TException;

public class MonitorServiceClient extends BaseServiceClient {

    public MonitorServiceClient() {
        super("com.isuwang.soa.monitor.api.service.MonitorService", "1.0.0");
    }


    /**
     * 上送QPS信息
     **/
    public void uploadQPSStat(java.util.List<com.isuwang.soa.monitor.api.domain.QPSStat> qpsStats) throws TException {
        initContext("uploadQPSStat");

        try {
            uploadQPSStat_args uploadQPSStat_args = new uploadQPSStat_args();
            uploadQPSStat_args.setQpsStats(qpsStats);


            uploadQPSStat_result response = sendBase(uploadQPSStat_args, new uploadQPSStat_result(), new UploadQPSStat_argsSerializer(), new UploadQPSStat_resultSerializer());


        } finally {
            destoryContext();
        }
    }

    /**
     * 上送平台处理数据
     **/
    public void uploadPlatformProcessData(java.util.List<com.isuwang.soa.monitor.api.domain.PlatformProcessData> platformProcessDatas) throws TException {
        initContext("uploadPlatformProcessData");

        try {
            uploadPlatformProcessData_args uploadPlatformProcessData_args = new uploadPlatformProcessData_args();
            uploadPlatformProcessData_args.setPlatformProcessDatas(platformProcessDatas);


            uploadPlatformProcessData_result response = sendBase(uploadPlatformProcessData_args, new uploadPlatformProcessData_result(), new UploadPlatformProcessData_argsSerializer(), new UploadPlatformProcessData_resultSerializer());


        } finally {
            destoryContext();
        }
    }

    /**
     * 上送DataSource信息
     **/
    public void uploadDataSourceStat(java.util.List<com.isuwang.soa.monitor.api.domain.DataSourceStat> dataSourceStat) throws TException {
        initContext("uploadDataSourceStat");

        try {
            uploadDataSourceStat_args uploadDataSourceStat_args = new uploadDataSourceStat_args();
            uploadDataSourceStat_args.setDataSourceStat(dataSourceStat);


            uploadDataSourceStat_result response = sendBase(uploadDataSourceStat_args, new uploadDataSourceStat_result(), new UploadDataSourceStat_argsSerializer(), new UploadDataSourceStat_resultSerializer());


        } finally {
            destoryContext();
        }
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
      