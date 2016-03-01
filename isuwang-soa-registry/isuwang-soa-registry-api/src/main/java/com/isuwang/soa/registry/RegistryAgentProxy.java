package com.isuwang.soa.registry;

/**
 * Registry Agent Proxy
 *
 * @author craneding
 * @date 16/3/1
 */
public class RegistryAgentProxy {
    private static RegistryAgent registryAgent = null;

    public static synchronized void loadImpl(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        if (RegistryAgentProxy.registryAgent != null)
            throw new RuntimeException("registry agent is exist.");

        RegistryAgentProxy.registryAgent = (RegistryAgent) clazz.newInstance();
    }

    public static RegistryAgent getCurrentInstance() {
        return RegistryAgentProxy.registryAgent;
    }

    public static void setCurrentInstance(RegistryAgent registryAgent) {
        RegistryAgentProxy.registryAgent = registryAgent;
    }
}
