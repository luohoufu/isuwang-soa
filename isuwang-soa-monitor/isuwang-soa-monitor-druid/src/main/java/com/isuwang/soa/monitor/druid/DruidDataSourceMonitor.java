package com.isuwang.soa.monitor.druid;

import com.alibaba.druid.stat.DruidStatManagerFacade;
import com.isuwang.soa.core.IPUtils;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.monitor.api.MonitorServiceClient;
import com.isuwang.soa.monitor.api.domain.DataSourceStat;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Druid Data Source Monitor
 *
 * @author craneding
 * @date 16/3/9
 */
public class DruidDataSourceMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DruidDataSourceMonitor.class);
    private static final AtomicInteger INDEX = new AtomicInteger(0);
    /**
     * 时间间隔(单位:秒)
     */
    private int period = 30;

    private final Timer timer = new Timer("DruidDataSourceMonitor-Timer-" + INDEX.incrementAndGet());

    public void init() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.SECOND, period);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    final long millis = System.currentTimeMillis() / 1000 * 1000;

                    final DruidStatManagerFacade facade = DruidStatManagerFacade.getInstance();

                    uploadDataSourceStat(millis, facade);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }, calendar.getTime(), period * 1000);
    }

    private void uploadDataSourceStat(long millis, DruidStatManagerFacade facade) throws TException {
        final List<DataSourceStat> stats = new ArrayList<>();

        final List<Map<String, Object>> dataSourceStatDataList = facade.getDataSourceStatDataList();

        for (Map<String, Object> map : dataSourceStatDataList) {
            final DataSourceStat stat = new DataSourceStat();

            stat.setPeriod(period);
            stat.setServerIP(IPUtils.localIp());
            stat.setServerPort(SoaSystemEnvProperties.SOA_CONTAINER_PORT);
            stat.setAnalysisTime(millis);
            stat.setUrl(String.valueOf(map.get("URL")));
            stat.setUserName(String.valueOf(map.get("UserName")));
            stat.setIdentity(String.valueOf(map.get("Identity")));
            stat.setDbType(String.valueOf(map.get("DbType")));
            stat.setPoolingCount(toInt(map.get("PoolingCount")));
            stat.setPoolingPeak(Optional.of(toInt(map.get("PoolingPeak"))));
            if (map.get("PoolingPeakTime") != null)
                stat.setPoolingPeakTime(Optional.of(((Date) map.get("PoolingPeakTime")).getTime()));
            stat.setActiveCount(toInt(map.get("ActiveCount")));
            stat.setActivePeak(Optional.of(toInt(map.get("ActivePeak"))));
            if (map.get("ActivePeakTime") != null)
                stat.setActivePeakTime(Optional.of(((Date) map.get("ActivePeakTime")).getTime()));
            stat.setExecuteCount(toInt(map.get("ExecuteCount")));
            stat.setErrorCount(toInt(map.get("ErrorCount")));

            stats.add(stat);
        }

        new MonitorServiceClient().uploadDataSourceStat(stats);
    }

    private int toInt(Object o) {
        if (o == null)
            return 0;

        return Integer.valueOf(o.toString());
    }

    public void destory() {
        timer.cancel();
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }
}
