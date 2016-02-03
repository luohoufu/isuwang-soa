package com.isuwang.soa.container.filter;

import com.isuwang.soa.container.Container;
import com.isuwang.soa.container.Main;
import com.isuwang.soa.container.filter.xml.SoaFilter;
import com.isuwang.soa.container.filter.xml.SoaFilters;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.container.ContainerFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter Container
 *
 * @author craneding
 * @date 16/2/3
 */
public class FilterContainer implements Container {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterContainer.class);

    private static List<Filter> filters = new ArrayList<>();

    @Override
    public void start() {
        try (InputStream is = new BufferedInputStream(Main.loadInputStreamInClassLoader("filters-server.xml"))) {
            SoaFilters soaFilters = JAXB.unmarshal(is, SoaFilters.class);
            for (SoaFilter soaFilter : soaFilters.getSoaFilter()) {

                Class filterClass = FilterContainer.class.getClassLoader().loadClass(soaFilter.getRef());
                Filter filter = (Filter) filterClass.newInstance();

                ContainerFilterChain.addFilter(filter);

                LOGGER.info("service load filter {} with path {}", soaFilter.getName(), soaFilter.getRef());

                filters.add(filter);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void stop() {
        filters.forEach(ContainerFilterChain::removeFilter);

        filters.clear();
    }

}
