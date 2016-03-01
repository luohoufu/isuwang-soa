//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.8-b130911.1802 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2016.03.01 时间 11:31:10 AM CST 
//


package com.isuwang.soa.remoting.conf;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.isuwang.soa.remoting.conf package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.isuwang.soa.remoting.conf
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SoaRemotingConnectionPool }
     * 
     */
    public SoaRemotingConnectionPool createSoaRemotingConnectionPool() {
        return new SoaRemotingConnectionPool();
    }

    /**
     * Create an instance of {@link SoaRemotingFilters }
     * 
     */
    public SoaRemotingFilters createSoaRemotingFilters() {
        return new SoaRemotingFilters();
    }

    /**
     * Create an instance of {@link SoaRemotingFilter }
     * 
     */
    public SoaRemotingFilter createSoaRemotingFilter() {
        return new SoaRemotingFilter();
    }

    /**
     * Create an instance of {@link SoaRemoting }
     * 
     */
    public SoaRemoting createSoaRemoting() {
        return new SoaRemoting();
    }

}
