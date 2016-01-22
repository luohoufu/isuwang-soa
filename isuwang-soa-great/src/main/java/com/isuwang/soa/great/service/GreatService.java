
package com.isuwang.soa.great.service;

import com.isuwang.soa.core.Processor;
import com.isuwang.soa.core.Service;

/**
 *
 **/
@Service(version = "1.0.0")
@Processor(className = "com.isuwang.soa.great.GreatServiceCodec$Processor")
public interface GreatService {

    /**
     *
     **/
    void sayGreat(String msg) throws com.isuwang.soa.core.SoaException;

}
        