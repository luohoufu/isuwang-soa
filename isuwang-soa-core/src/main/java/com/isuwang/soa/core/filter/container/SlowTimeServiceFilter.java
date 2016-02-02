package com.isuwang.soa.core.filter.container;

import com.isuwang.soa.core.Context;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import org.apache.thrift.TException;

/**
 * Created by tangliu on 2016/2/1.
 */
public class SlowTimeServiceFilter implements Filter {

    private final TaskManager taskManager = new TaskManager();

    @Override
    public void doFilter(FilterChain chain) throws TException {

        if (!taskManager.hasStarted()) {
            taskManager.start();
        }

        Context context = Context.Factory.getCurrentInstance();
        Task task = new Task(context);
        taskManager.addTask(task);

        try {
            chain.doFilter();
        } finally {
            taskManager.remove(task);
        }
    }

}
