package com.isuwang.soa.maven.plugin;

import com.isuwang.soa.bootstrap.Bootstrap;
import com.isuwang.soa.bootstrap.classloader.AppClassLoader;
import com.isuwang.soa.bootstrap.classloader.ClassLoaderManager;
import com.isuwang.soa.bootstrap.classloader.PlatformClassLoader;
import com.isuwang.soa.bootstrap.classloader.ShareClassLoader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

        System.setProperty("soa.base", new File(project.getBuild().getOutputDirectory()).getAbsolutePath().replace("/target/classes", ""));
        System.setProperty("soa.run.mode", "maven");

        final String mainClass = Bootstrap.class.getName();

        IsolatedThreadGroup threadGroup = new IsolatedThreadGroup(mainClass);
        Thread bootstrapThread = new Thread(threadGroup, () -> {
            try {
                //ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

                URL[] urls = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();

                List<URL> urlList = new ArrayList<>(Arrays.asList(urls));
                Iterator<URL> iterator = urlList.iterator();
                while (iterator.hasNext()) {
                    URL url = iterator.next();

                    if (url.getFile().matches("^.*/isuwang-soa-container.*\\.jar$")) {
                        iterator.remove();

                        continue;
                    }

                    if (url.getFile().matches("^.*/isuwang-soa-bootstrap.*\\.jar$")) {
                        iterator.remove();

                        continue;
                    }
                }

                ClassLoaderManager.shareClassLoader = new ShareClassLoader(urls);
                ClassLoaderManager.platformClassLoader = new PlatformClassLoader(urls);
                ClassLoaderManager.appClassLoaders.add(new AppClassLoader(urlList.toArray(new URL[urlList.size()])));

                Bootstrap.main(new String[]{});
            } catch (Exception e) {
                Thread.currentThread().getThreadGroup().uncaughtException(Thread.currentThread(), e);
            }
        }, mainClass + ".main()");
        bootstrapThread.setContextClassLoader(getClassLoader());
        bootstrapThread.start();

        joinNonDaemonThreads(threadGroup);
    }

}
