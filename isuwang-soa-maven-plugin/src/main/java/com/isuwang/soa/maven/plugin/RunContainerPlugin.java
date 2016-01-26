package com.isuwang.soa.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

/**
 * Run Container Plugin
 *
 * @author craneding
 * @date 16/1/25
 */
@Mojo(name = "run")
public class RunContainerPlugin extends AbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        Map pluginContext = getPluginContext();

        if (pluginContext == null)
            throw new MojoExecutionException("not found project.");

        getLog().info(pluginContext.toString());

        MavenProject project = (MavenProject) pluginContext.get("project");

        if (project == null)
            throw new MojoExecutionException("not found project.");

        getLog().info("bundle:" + project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());


        try {
            URL respository = new URL("file", null, "/Users/craneding/git/isuwang-soa/isuwang-soa-container/target/classes/").toURI().toURL();

            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{respository});

            Class<?> aClass = urlClassLoader.loadClass("com.isuwang.soa.container.Main");

            Method mainMethod = aClass.getMethod("main", String[].class);

            mainMethod.invoke(aClass, new Object[]{new String[]{}});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
