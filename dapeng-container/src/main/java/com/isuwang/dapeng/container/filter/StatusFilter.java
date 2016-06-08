package com.isuwang.dapeng.container.filter;

import com.isuwang.dapeng.core.filter.Filter;

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
