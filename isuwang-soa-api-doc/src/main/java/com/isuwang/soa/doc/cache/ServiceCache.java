package com.isuwang.soa.doc.cache;


import com.google.common.collect.TreeMultimap;
import com.isuwang.soa.code.generator.MetadataGenerator;
import com.isuwang.soa.code.generator.metadata.*;
import com.isuwang.soa.code.parser.ThriftCodeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * Service Cache
 *
 * @author craneding
 * @date 15/4/26
 */
public class ServiceCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCache.class);

    private static Map<String, Service> services = new TreeMap<>();

    public static TreeMultimap<String, String> urlMappings = TreeMultimap.create();

    public void init() {

        final File classesDir = new File(ServiceCache.class.getClassLoader().getResource("./").getPath());
        final File metaHomeDir = new File(classesDir, "meta-xml");
        if (metaHomeDir.exists()) {
            File[] files = metaHomeDir.listFiles();
            if (files != null)
                for (File file : files) {
                    LOGGER.info("delete cache file:{} {}", file.getName(), file.delete());
                }

            LOGGER.info("delete cache dir:{} {}", metaHomeDir.getName(), metaHomeDir.delete());
        }

        metaHomeDir.mkdirs();

        File[] thriftFiles = classesDir.listFiles((dir, name) -> {
            return name.matches("^.*[.]thrift$");
        });

        List<String> resource = new ArrayList<>();
        for (File thriftFile : thriftFiles) {
            resource.add(thriftFile.getAbsolutePath());
        }

        List<Service> servicesList = new ThriftCodeParser().toServices(resource.toArray(new String[resource.size()]));
        new MetadataGenerator().generate(servicesList, metaHomeDir.getAbsolutePath());

        String[] metaFiles = metaHomeDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("^.*[.][xX][mM][lL]$");
            }
        });

        final Map<String, Service> services = new TreeMap<>();

        urlMappings.clear();

        for (String metaFile : metaFiles) {
            LOGGER.info("load meta: {}", metaFile);

            loadResource(metaFile, services);
        }

        this.services = services;

        LOGGER.info("size of urlMapping: " + urlMappings.size());
    }

    public void destory() {
        services.clear();
    }

    public void loadResource(String name, Map<String, Service> services) {
        try (BufferedInputStream inputStream = new BufferedInputStream(ServiceCache.class.getClassLoader().getResourceAsStream("./meta-xml/" + name))) {
            Service service = JAXB.unmarshal(inputStream, Service.class);

            String key = getKey(service);

            services.put(key, service);

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

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public Service getService(String name, String version) {
        return services.get(getKey(name, version));
    }

    private String getKey(Service service) {
        return getKey(service.getName(), service.getMeta().version);
    }

    private String getKey(String name, String version) {
        return name + ":" + version;
    }

    public Map<String, Service> getServices() {
        return services;
    }

}
