package com.isuwang.soa.great;

import com.isuwang.soa.core.SoaException;
import com.isuwang.soa.great.service.GreatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tangliu on 2016/1/18.
 */
public class GreatServiceImpl implements GreatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreatServiceImpl.class);

    @Override
    public void sayGreat(String msg) throws SoaException {
        LOGGER.info("great!{}", msg);
    }

}
