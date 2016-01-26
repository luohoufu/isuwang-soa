//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.8-b130911.1802 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2016.01.26 时间 05:03:16 PM CST
//


package com.isuwang.soa.container.xml;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>anonymous complex type的 Java 类。
 * <p>
 * <p>以下模式片段指定包含在此类中的预期内容。
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}soa-container" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "soaContainer"
})
@XmlRootElement(name = "soa-containers")
public class SoaContainers {

    @XmlElement(name = "soa-container", required = true)
    protected List<SoaContainer> soaContainer;

    /**
     * Gets the value of the soaContainer property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the soaContainer property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSoaContainer().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SoaContainer }
     */
    public List<SoaContainer> getSoaContainer() {
        if (soaContainer == null) {
            soaContainer = new ArrayList<SoaContainer>();
        }
        return this.soaContainer;
    }

}
