package com.isuwang.soa.spring;

import com.isuwang.soa.core.Processor;
import com.isuwang.soa.core.Service;
import com.isuwang.soa.core.SoaBaseProcessor;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Soa Processor Factory
 *
 * @author craneding
 * @date 16/1/19
 */
public class SoaProcessorFactory implements FactoryBean<SoaBaseProcessor<?>> {

    private Object serviceRef;
    private String refId;

    public SoaProcessorFactory(Object serviceRef, String refId) {
        this.serviceRef = serviceRef;
        this.refId = refId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SoaBaseProcessor<?> getObject() throws Exception {
        final Class<?> aClass = serviceRef.getClass();
        final List<Class<?>> interfaces = Arrays.asList(aClass.getInterfaces());

        List<Class<?>> filterInterfaces = interfaces.stream()
                .filter(anInterface -> anInterface.isAnnotationPresent(Service.class) && anInterface.isAnnotationPresent(Processor.class))
                .map(anInterface -> anInterface)
                .collect(toList());

        if (filterInterfaces.isEmpty())
            throw new RuntimeException("not config @Service & @Processor in " + refId);

        Class<?> interfaceClass = filterInterfaces.get(filterInterfaces.size() - 1);

        Processor processor = interfaceClass.getAnnotation(Processor.class);

        Class<?> processorClass = Class.forName(processor.className(), true, interfaceClass.getClassLoader());
        Constructor<?> constructor = processorClass.getConstructor(interfaceClass);
        SoaBaseProcessor soaBaseProcessor = (SoaBaseProcessor) constructor.newInstance(serviceRef);

        soaBaseProcessor.setInterfaceClass(interfaceClass);

        return soaBaseProcessor;
    }

    @Override
    public Class<?> getObjectType() {
        return SoaBaseProcessor.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
