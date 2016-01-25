package com.isuwang.soa.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

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
        getLog().info("hello world.");

        Map pluginContext = getPluginContext();

        getLog().info(pluginContext.toString());
    }

}
