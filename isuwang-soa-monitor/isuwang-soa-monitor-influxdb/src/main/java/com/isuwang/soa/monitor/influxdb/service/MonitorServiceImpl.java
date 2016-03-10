package com.isuwang.soa.monitor.influxdb.service;

import com.isuwang.soa.core.SoaException;
import com.isuwang.soa.monitor.api.domain.DataSourceStat;
import com.isuwang.soa.monitor.api.domain.PlatformProcessData;
import com.isuwang.soa.monitor.api.domain.QPSStat;
import com.isuwang.soa.monitor.api.service.MonitorService;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Monitor Service Impl(Influxdb)
 *
 * @author craneding
 * @date 16/3/8
 */
public class MonitorServiceImpl implements MonitorService {

    private String url = "http://192.168.99.100:8886";
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

        double value = 0.0;

        if (qpsStat.getCallCount() != 0)// value = callcount / period
            value = new BigDecimal(qpsStat.getCallCount().toString()).divide(new BigDecimal(qpsStat.getPeriod().toString()), BigDecimal.ROUND_DOWN).doubleValue();

        Point point = Point.measurement("qps")
                .time(qpsStat.getAnalysisTime(), TimeUnit.MILLISECONDS)
                .field("value", value)
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
                    .tag("service_name", processData.getServiceName())
                    .tag("method_name", processData.getMethodName())
                    .tag("version_name", processData.getVersionName())
                    .tag("server_ip", processData.getServerIP())
                    .tag("server_port", processData.getServerPort().toString())
                    .time(processData.getAnalysisTime(), TimeUnit.MILLISECONDS)
                    .field("p_mintime", processData.getPMinTime())
                    .field("p_maxTime", processData.getPMaxTime())
                    .field("p_averagetime", processData.getPAverageTime())
                    .field("p_totaltime", processData.getPTotalTime())
                    .field("i_mintime", processData.getIMinTime())
                    .field("i_maxtime", processData.getIMaxTime())
                    .field("i_averagetime", processData.getIAverageTime())
                    .field("i_totaltime", processData.getITotalTime())
                    .field("total_calls", processData.getTotalCalls())
                    .field("succeed_calls", processData.getSucceedCalls())
                    .field("fail_calls", processData.getFailCalls())
                    .field("request_flow", processData.getRequestFlow())
                    .field("response_flow", processData.getResponseFlow())
                    .build();

            batchPoints.point(point);
        }

        influxDB.write(batchPoints);
    }

    @Override
    public void uploadDataSourceStat(List<DataSourceStat> dataSourceStat) throws SoaException {
        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

        for (DataSourceStat stat : dataSourceStat) {
            Point point = Point.measurement("datasource_stat")
                    .tag("period", stat.getPeriod().toString())
                    .tag("server_ip", stat.getServerIP())
                    .tag("server_port", stat.getServerPort().toString())
                    .tag("url", stat.getUrl())
                    .tag("user_name", stat.getUserName())
                    .tag("identity", stat.getIdentity())
                    .tag("db_type", stat.getDbType())
                    .tag("pooling_peaktime", stat.getPoolingPeakTime().isPresent() ? stat.getPoolingPeakTime().get().toString() : "-")
                    .tag("active_peaktime", stat.getActivePeakTime().isPresent() ? stat.getActivePeakTime().get().toString() : "-")
                    .time(stat.getAnalysisTime(), TimeUnit.MILLISECONDS)
                    .field("pooling_count", stat.getPoolingCount())
                    .field("active_count", stat.getActiveCount())
                    .field("execute_count", stat.getExecuteCount())
                    .field("error_count", stat.getErrorCount())
                    .field("pooling_peak", stat.getPoolingPeak().isPresent() ? stat.getPoolingPeak().get() : 0)
                    .field("active_peak", stat.getActivePeak().isPresent() ? stat.getActivePeak().get() : 0)
                    .build();

            batchPoints.point(point);
        }

        influxDB.write(batchPoints);
    }

}
