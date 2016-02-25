package com.isuwang.soa.code.generator.metadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Method {

    @XmlAttribute
    public String name;
    public String doc;
    public String label;

    public Struct request;
    public Struct response;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Struct getRequest() {
        return request;
    }

    public void setRequest(Struct request) {
        this.request = request;
    }

    public Struct getResponse() {
        return response;
    }

    public void setResponse(Struct response) {
        this.response = response;
    }

}
