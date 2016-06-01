package com.isuwang.soa.core.filter;

import com.isuwang.org.apache.thrift.TException;

/**
 * Filter Chain
 *
 * @author craneding
 * @date 16/1/20
 */
public interface FilterChain {

    void doFilter() throws TException;

    Object getAttribute(String name);

    void setAttribute(String name, Object value);
}
