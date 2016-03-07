package com.isuwang.soa.doc.cache;


import com.google.common.collect.TreeMultimap;
import com.isuwang.soa.core.SoaBaseProcessor;
import com.isuwang.soa.core.metadata.*;
import com.isuwang.soa.registry.RegistryAgentProxy;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Service Cache
 *
 * @author craneding
 * @date 15/4/26
 */
public class ServiceCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCache.class);

    private static Map<String, Service> services = new TreeMap<>();

    private static Map<String, Service> fullNameService = new TreeMap<>();

    public static TreeMultimap<String, String> urlMappings = TreeMultimap.create();

    public void init() {

        final Map<String, Service> services = new TreeMap<>();
        urlMappings.clear();

        Map<String, SoaBaseProcessor<?>> processorMap = RegistryAgentProxy.getCurrentInstance().getProcessorMap();

        Set<String> keys = processorMap.keySet();
        for (String key : keys) {
            SoaBaseProcessor<?> processor = processorMap.get(key);
            if (processor.getInterfaceClass().getClass() != null) {

                com.isuwang.soa.core.Service service = processor.getInterfaceClass().getAnnotation(com.isuwang.soa.core.Service.class);

                String serviceName = processor.getInterfaceClass().getName();
                String version = service.version();


                String metadata = "";
                try {
                    metadata = new MetadataClient(serviceName, version).getServiceMetadata();
                } catch (TException e) {
                    LOGGER.error(e.getMessage(), e);
                }

                try (StringReader reader = new StringReader(metadata)) {
                    Service serviceData = JAXB.unmarshal(reader, Service.class);
                    loadResource(serviceData, services);
                } catch (Exception e) {
                    LOGGER.error("生成SERVICE出错", e);
                }
            }
        }
        this.services = services;

        LOGGER.info("size of urlMapping: " + urlMappings.size());
    }

    public void destory() {
        services.clear();
    }

    public void loadResource(Service service, Map<String, Service> services) {

        String key = getKey(service);
        services.put(key, service);

        String fullNameKey = getFullNameKey(service);
        fullNameService.put(fullNameKey, service);

        //将service和service中的方法、结构体、枚举和字段名分别设置对应的url，以方便搜索
        urlMappings.put(service.getName(), "api/service/" + service.name + "/" + service.meta.version + ".htm");
        List<Method> methods = service.getMethods();
        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            urlMappings.put(method.name, "api/method/" + service.name + "/" + service.meta.version + "/" + method.name + ".htm");
        }

        List<Struct> structs = service.getStructDefinitions();
        for (int i = 0; i < structs.size(); i++) {
            Struct struct = structs.get(i);
            urlMappings.put(struct.name, "api/struct/" + service.name + "/" + service.meta.version + "/" + struct.namespace + "." + struct.name + ".htm");

            List<Field> fields = struct.getFields();
            for (int j = 0; j < fields.size(); j++) {
                Field field = fields.get(j);
                urlMappings.put(field.name, "api/struct/" + service.name + "/" + service.meta.version + "/" + struct.namespace + "." + struct.name + ".htm");
            }
        }

        List<TEnum> tEnums = service.getEnumDefinitions();
        for (int i = 0; i < tEnums.size(); i++) {
            TEnum tEnum = tEnums.get(i);
            urlMappings.put(tEnum.name, "api/enum/" + service.name + "/" + service.meta.version + "/" + tEnum.namespace + "." + tEnum.name + ".htm");
        }

    }

    public Service getService(String name, String version) {

        if (name.contains("."))
            return fullNameService.get(getKey(name, version));
        else
            return services.get(getKey(name, version));
    }

    private String getKey(Service service) {
        return getKey(service.getName(), service.getMeta().version);
    }

    private String getFullNameKey(Service service) {
        return getKey(service.getNamespace() + "." + service.getName(), service.getMeta().version);
    }

    private String getKey(String name, String version) {
        return name + ":" + version;
    }

    public Map<String, Service> getServices() {
        return services;
    }

}
