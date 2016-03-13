package com.isuwang.soa.container.registry;

import com.isuwang.soa.core.SoaBaseProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author craneding
 * @date 16/3/13
 */
public class ProcessorCache {

    private static final Map<String, SoaBaseProcessor<?>> processorMap = new ConcurrentHashMap<>();

    public static Map<String, SoaBaseProcessor<?>> getProcessorMap() {
        return processorMap;
    }

}
