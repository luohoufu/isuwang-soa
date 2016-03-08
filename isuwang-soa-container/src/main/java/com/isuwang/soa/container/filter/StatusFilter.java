package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.filter.Filter;

/**
 * Status Filter
 *
 * @author craneding
 * @date 16/3/7
 */
public interface StatusFilter extends Filter {

    void init();

    void destory();

}
