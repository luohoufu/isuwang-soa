package com.isuwang.soa.maven.plugin;

import com.isuwang.soa.engine.Engine;
import com.isuwang.soa.engine.classloader.AppClassLoader;
import com.isuwang.soa.engine.classloader.ClassLoaderManager;
import com.isuwang.soa.engine.classloader.PlatformClassLoader;
import com.isuwang.soa.engine.classloader.ShareClassLoader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Run Container Plugin
 *
 * @author craneding
 * @date 16/1/25
 */
@Mojo(name = "run", threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class RunContainerPlugin extends SoaAbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (project == null)
            throw new MojoExecutionException("not found project.");

        getLog().info("bundle:" + project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());

        final String mainClass = Engine.class.getName();

        IsolatedThreadGroup threadGroup = new IsolatedThreadGroup(mainClass);
        Thread bootstrapThread = new Thread(threadGroup, new Runnable() {
            public void run() {
                try {
                    //ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

                    URL[] urls = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();

                    ClassLoaderManager.shareClassLoader = new ShareClassLoader(urls);
                    ClassLoaderManager.platformClassLoader = new PlatformClassLoader(urls);
                    ClassLoaderManager.appClassLoaders.add(new AppClassLoader(urls));

                    Engine.main(new String[]{});
                } catch (Exception e) {
                    Thread.currentThread().getThreadGroup().uncaughtException(Thread.currentThread(), e);
                }
            }
        }, mainClass + ".main()");
        bootstrapThread.setContextClassLoader(getClassLoader());
        bootstrapThread.start();

        joinNonDaemonThreads(threadGroup);
    }

}
