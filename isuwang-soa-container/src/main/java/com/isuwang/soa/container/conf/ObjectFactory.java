//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.8-b130911.1802 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2016.03.01 时间 02:42:48 PM CST 
//


package com.isuwang.soa.container.conf;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.isuwang.soa.container.conf package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.isuwang.soa.container.conf
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SoaServerContainer }
     * 
     */
    public SoaServerContainer createSoaServerContainer() {
        return new SoaServerContainer();
    }

    /**
     * Create an instance of {@link SoaServerFilter }
     * 
     */
    public SoaServerFilter createSoaServerFilter() {
        return new SoaServerFilter();
    }

    /**
     * Create an instance of {@link SoaFilters }
     * 
     */
    public SoaFilters createSoaFilters() {
        return new SoaFilters();
    }

    /**
     * Create an instance of {@link SoaServer }
     * 
     */
    public SoaServer createSoaServer() {
        return new SoaServer();
    }

    /**
     * Create an instance of {@link SoaServerContainers }
     * 
     */
    public SoaServerContainers createSoaServerContainers() {
        return new SoaServerContainers();
    }

}
