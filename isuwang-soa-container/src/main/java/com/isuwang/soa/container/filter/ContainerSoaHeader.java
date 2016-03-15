package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.HeaderProxy;
import com.isuwang.soa.core.IPUtils;
import com.isuwang.soa.core.InvocationContext;
import com.isuwang.soa.core.SoaSystemEnvProperties;

/**
 * Container SoaHeader
 *
 * @author craneding
 * @date 16/3/15
 */
public class ContainerSoaHeader {

    private static HeaderProxy headerProxy = null;

    public static void setup() {
        if (headerProxy != null)
            return;

        headerProxy = new HeaderProxy();

        headerProxy.setCallerFrom("soaServer:" + IPUtils.localIp() + ":" + SoaSystemEnvProperties.SOA_CONTAINER_PORT);

        InvocationContext.Factory.setSoaHeaderProxy(headerProxy);
    }

}
