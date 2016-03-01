//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.8-b130911.1802 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2016.03.01 时间 11:31:10 AM CST 
//


package com.isuwang.soa.remoting.conf;

import javax.xml.bind.annotation.*;


/**
 * <p>anonymous complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}soa-remoting-filters"/>
 *         &lt;element ref="{}soa-remoting-connection-pool"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "soaRemotingFilters",
    "soaRemotingConnectionPool"
})
@XmlRootElement(name = "soa-remoting")
public class SoaRemoting {

    @XmlElement(name = "soa-remoting-filters", required = true)
    protected SoaRemotingFilters soaRemotingFilters;
    @XmlElement(name = "soa-remoting-connection-pool", required = true)
    protected SoaRemotingConnectionPool soaRemotingConnectionPool;

    /**
     * 获取soaRemotingFilters属性的值。
     * 
     * @return
     *     possible object is
     *     {@link SoaRemotingFilters }
     *     
     */
    public SoaRemotingFilters getSoaRemotingFilters() {
        return soaRemotingFilters;
    }

    /**
     * 设置soaRemotingFilters属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link SoaRemotingFilters }
     *     
     */
    public void setSoaRemotingFilters(SoaRemotingFilters value) {
        this.soaRemotingFilters = value;
    }

    /**
     * 获取soaRemotingConnectionPool属性的值。
     * 
     * @return
     *     possible object is
     *     {@link SoaRemotingConnectionPool }
     *     
     */
    public SoaRemotingConnectionPool getSoaRemotingConnectionPool() {
        return soaRemotingConnectionPool;
    }

    /**
     * 设置soaRemotingConnectionPool属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link SoaRemotingConnectionPool }
     *     
     */
    public void setSoaRemotingConnectionPool(SoaRemotingConnectionPool value) {
        this.soaRemotingConnectionPool = value;
    }

}
