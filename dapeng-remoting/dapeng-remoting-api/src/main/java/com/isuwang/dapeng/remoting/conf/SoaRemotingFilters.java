//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.8-b130911.1802 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2016.03.01 时间 11:31:10 AM CST 
//


package com.isuwang.dapeng.remoting.conf;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


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
 *         &lt;element ref="{}soa-remoting-filter" maxOccurs="unbounded"/>
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
    "soaRemotingFilter"
})
@XmlRootElement(name = "soa-remoting-filters")
public class SoaRemotingFilters {

    @XmlElement(name = "soa-remoting-filter", required = true)
    protected List<SoaRemotingFilter> soaRemotingFilter;

    /**
     * Gets the value of the soaRemotingFilter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the soaRemotingFilter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSoaRemotingFilter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SoaRemotingFilter }
     * 
     * 
     */
    public List<SoaRemotingFilter> getSoaRemotingFilter() {
        if (soaRemotingFilter == null) {
            soaRemotingFilter = new ArrayList<SoaRemotingFilter>();
        }
        return this.soaRemotingFilter;
    }

}
