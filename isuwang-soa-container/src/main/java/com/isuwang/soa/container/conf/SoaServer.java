//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.8-b130911.1802 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2016.03.01 时间 02:42:48 PM CST 
//


package com.isuwang.soa.container.conf;

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
 *         &lt;element ref="{}soa-server-containers"/>
 *         &lt;element ref="{}soa-filters"/>
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
    "soaServerContainers",
    "soaFilters"
})
@XmlRootElement(name = "soa-server")
public class SoaServer {

    @XmlElement(name = "soa-server-containers", required = true)
    protected SoaServerContainers soaServerContainers;
    @XmlElement(name = "soa-filters", required = true)
    protected SoaFilters soaFilters;

    /**
     * 获取soaServerContainers属性的值。
     * 
     * @return
     *     possible object is
     *     {@link SoaServerContainers }
     *     
     */
    public SoaServerContainers getSoaServerContainers() {
        return soaServerContainers;
    }

    /**
     * 设置soaServerContainers属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link SoaServerContainers }
     *     
     */
    public void setSoaServerContainers(SoaServerContainers value) {
        this.soaServerContainers = value;
    }

    /**
     * 获取soaFilters属性的值。
     * 
     * @return
     *     possible object is
     *     {@link SoaFilters }
     *     
     */
    public SoaFilters getSoaFilters() {
        return soaFilters;
    }

    /**
     * 设置soaFilters属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link SoaFilters }
     *     
     */
    public void setSoaFilters(SoaFilters value) {
        this.soaFilters = value;
    }

}
