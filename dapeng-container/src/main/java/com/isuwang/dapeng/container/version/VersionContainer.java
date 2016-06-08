package com.isuwang.dapeng.container.version;

import com.isuwang.dapeng.container.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Version Filter
 *
 * @author craneding
 * @date 16/2/1
 */
public class VersionContainer implements Container {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionContainer.class);

    private boolean logArgs = true;
    private boolean logEnv = false;
    private boolean logProps = false;

    @Override
    public void start() {
        if (logArgs) {
            List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
            for (String arg : args) {
                LOGGER.info("arg {}", arg);
            }
        }

        if (logEnv) {
            SortedMap<String, String> sortedMap = new TreeMap<>(System.getenv());
            for (Map.Entry<String, String> e : sortedMap.entrySet()) {
                LOGGER.info("env {} {}", e.getKey(), e.getValue());
            }
        }

        if (logProps) {
            SortedMap<String, String> sortedMap = new TreeMap<>();
            for (Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
                sortedMap.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
            }
            for (Map.Entry<String, String> e : sortedMap.entrySet()) {
                LOGGER.info("prop {} {}", e.getKey(), e.getValue());
            }
        }

    }

    public void setLogArgs(boolean logArgs) {
        this.logArgs = logArgs;
    }

    public void setLogProps(boolean logProps) {
        this.logProps = logProps;
    }

    public void setLogEnv(boolean logEnv) {
        this.logEnv = logEnv;
    }

    @Override
    public void stop() {

    }
}
