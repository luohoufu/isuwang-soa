package com.isuwang.soa.engine.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Platform Class Loader
 *
 * @author craneding
 * @date 16/1/28
 */
public class PlatformClassLoader extends URLClassLoader {

    public PlatformClassLoader(URL[] urls) {
        super(urls, ClassLoaderManager.shareClassLoader);
    }

}
