package com.isuwang.soa.monitor.influxdb.service;

import com.isuwang.soa.core.SoaException;
import com.isuwang.soa.monitor.api.domain.PlatformProcessData;
import com.isuwang.soa.monitor.api.domain.QPSStat;
import com.isuwang.soa.monitor.api.service.MonitorService;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Monitor Service Impl(Influxdb)
 *
 * @author craneding
 * @date 16/3/8
 */
public class MonitorServiceImpl implements MonitorService {

    private String url = "http://192.168.99.100:8086";
    private String userName = "root";
    private String password = "root";

    private String dbName = "soadb";
    private InfluxDB influxDB = InfluxDBFactory.connect(url, userName, password);

    @Override
    public void uploadQPSStat(QPSStat qpsStat) throws SoaException {
        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .tag("server_ip", qpsStat.getServerIP())
                .tag("server_port", qpsStat.getServerPort().toString())
                .tag("period", qpsStat.getPeriod().toString())
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

        Point point = Point.measurement("qps")
                .time(qpsStat.getAnalysisTime(), TimeUnit.MILLISECONDS)
                .field("value", qpsStat.getCallCount() / qpsStat.getPeriod())
                .build();

        batchPoints.point(point);

        influxDB.write(batchPoints);
    }

    @Override
    public void uploadPlatformProcessData(List<PlatformProcessData> platformProcessDatas) throws SoaException {
        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

        for (PlatformProcessData processData : platformProcessDatas) {
            Point point = Point.measurement("platform_process")
                    .tag("period", processData.getPeriod().toString())
                    .tag("serviceName", processData.getServiceName())
                    .tag("methodName", processData.getMethodName())
                    .tag("versionName", processData.getVersionName())
                    .tag("server_ip", processData.getServerIP())
                    .tag("server_port", processData.getServerPort().toString())
                    .time(processData.getAnalysisTime(), TimeUnit.MILLISECONDS)
                    .field("pMinTime", processData.getPMinTime())
                    .field("pMaxTime", processData.getPMaxTime())
                    .field("pAverageTime", processData.getPAverageTime())
                    .field("pTotalTime", processData.getPTotalTime())
                    .field("iMinTime", processData.getIMinTime())
                    .field("iMaxTime", processData.getIMaxTime())
                    .field("iAverageTime", processData.getIAverageTime())
                    .field("iTotalTime", processData.getITotalTime())
                    .field("totalCalls", processData.getTotalCalls())
                    .field("succeedCalls", processData.getSucceedCalls())
                    .field("failCalls", processData.getFailCalls())
                    .field("requestFlow", processData.getRequestFlow())
                    .field("responseFlow", processData.getResponseFlow())
                    .build();

            batchPoints.point(point);
        }

        influxDB.write(batchPoints);
    }

}
