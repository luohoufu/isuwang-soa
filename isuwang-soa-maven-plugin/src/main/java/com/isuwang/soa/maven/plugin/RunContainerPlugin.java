package com.isuwang.soa.maven.plugin;

import com.isuwang.soa.container.Main;
import com.isuwang.soa.container.spring.SpringContainer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

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

        final String mainClass = Main.class.getName();

        IsolatedThreadGroup threadGroup = new IsolatedThreadGroup(mainClass);
        Thread bootstrapThread = new Thread(threadGroup, new Runnable() {
            public void run() {
                try {
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

                    SpringContainer.appClassLoaders = new ArrayList<ClassLoader>(Arrays.asList(contextClassLoader));

                    Class<?> mainClass = contextClassLoader.loadClass(Main.class.getName());

                    Method mainMethod = mainClass.getMethod("main", new Class<?>[]{String[].class});

                    mainMethod.invoke(mainClass, new Object[]{new String[]{}});
                    //Main.main(new String[]{});
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
