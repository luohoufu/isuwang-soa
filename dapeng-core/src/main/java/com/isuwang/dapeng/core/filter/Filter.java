package com.isuwang.dapeng.core.filter;

import com.isuwang.org.apache.thrift.TException;

/**
 * Filter
 *
 * @author craneding
 * @date 16/1/20
 */
public interface Filter {

    //void init();

    void doFilter(FilterChain chain) throws TException;

    //void destory();

}
