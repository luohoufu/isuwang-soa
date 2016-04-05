package com.isuwang.soa.container.filter;

import com.isuwang.soa.container.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tangliu on 2016/2/1.
 */
public class TaskManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerUtil.SLOWTIME_LOG);

    private boolean live = false;

    private List<Task> tasks = Collections.synchronizedList(new ArrayList<Task>());

    private Map<Thread, String> lastStackInfo = new ConcurrentHashMap<>();

    private static final long DEFAULT_SLEEP_TIME = 3000L;

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void remove(Task task) {
        lastStackInfo.remove(task.getCurrentThread());
        tasks.remove(task);
    }

    public boolean hasStarted() {
        return live;
    }

    public void start() {
        live = true;

        final Thread targetThread = new Thread("Check task time Thread") {
            @Override
            public void run() {
                while (live) {
                    try {
                        checkSampleTask();
                    } catch (Exception e) {
                        LOGGER.error("Check task time thread error", e);
                    }
                }
            }
        };
        targetThread.start();
    }

    public void stop() {
        live = false;
        tasks.clear();
    }

    protected void checkSampleTask() throws InterruptedException {

        final List<Task> tasksCopy = new ArrayList<>(tasks);
        final Iterator<Task> iterator = tasksCopy.iterator();

        while (iterator.hasNext()) {
            final long currentTime = System.currentTimeMillis();
            final Task task = (Task) iterator.next();

            //if task being executing takes to much time, make a record.
            if (currentTime - task.getStartTime() >= (10 * 1000)) {

                LOGGER.info("Request has been processed exceed specify time:{},{},{},{},{},{},{},{},{},{}", task.getSeqid(), task.getServiceName(), task.getVersionName(),
                        task.getMethodName(), task.getCallerFrom(), task.getCallerIp(), task.getOperatorId(), task.getOperatorName(), task.getCustomerId(), task.getCustomerName());

                StackTraceElement[] stackElements = task.getCurrentThread().getStackTrace();

                if (stackElements != null && stackElements.length > 0) {

                    String firstStackInfo = stackElements[0].getClassName() + "." + stackElements[0].getMethodName() + "(" + stackElements[0].getFileName() + ":" + stackElements[0].getLineNumber() + ")";
                    if (lastStackInfo.containsKey(task.getCurrentThread()) && lastStackInfo.get(task.getCurrentThread()).equals(firstStackInfo))
                        LOGGER.info("Same as last check...");
                    else {
                        lastStackInfo.put(task.getCurrentThread(), firstStackInfo);
                        for (int i = 0; i < stackElements.length; i++) {
                            LOGGER.info(stackElements[i].getClassName() + "." + stackElements[i].getMethodName() + "(" + stackElements[i].getFileName() + ":" + stackElements[i].getLineNumber() + ")");
                        }
                    }
                }
            }
        }

        tasksCopy.clear();

        Thread.sleep(DEFAULT_SLEEP_TIME);
    }
}
