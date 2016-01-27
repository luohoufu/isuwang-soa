package com.isuwang.soa.maven.plugin;

import com.isuwang.soa.container.Main;
import com.isuwang.soa.container.spring.SpringContainer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Run Container Plugin
 *
 * @author craneding
 * @date 16/1/25
 */
@Mojo(name = "run", threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class RunContainerPlugin extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (project == null)
            throw new MojoExecutionException("not found project.");

        getLog().info("bundle:" + project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());

        try {
            SpringContainer.appClassLoaders = new ArrayList<ClassLoader>(Arrays.asList(getClassLoader()));
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        }

        Main.main(new String[]{});
    }

    private ClassLoader getClassLoader() throws MojoExecutionException {
        List<URL> classpathURLs = new ArrayList<URL>();
        //this.addRelevantPluginDependenciesToClasspath( classpathURLs );
        this.addRelevantProjectDependenciesToClasspath(classpathURLs);
        //this.addAdditionalClasspathElements( classpathURLs );
        return new URLClassLoader(classpathURLs.toArray(new URL[classpathURLs.size()]));
    }

    private void addRelevantProjectDependenciesToClasspath(List<URL> path) throws MojoExecutionException {
        try {
            getLog().debug("Project Dependencies will be included.");

            List<Artifact> artifacts = new ArrayList<Artifact>();
            List<File> theClasspathFiles = new ArrayList<File>();

            collectProjectArtifactsAndClasspath(artifacts, theClasspathFiles);

            for (File classpathFile : theClasspathFiles) {
                URL url = classpathFile.toURI().toURL();
                getLog().debug("Adding to classpath : " + url);
                path.add(url);
            }

            for (Artifact classPathElement : artifacts) {
                getLog().debug("Adding project dependency artifact: " + classPathElement.getArtifactId()
                        + " to classpath");
                path.add(classPathElement.getFile().toURI().toURL());
            }

        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Error during setting up classpath", e);
        }
    }

    private void collectProjectArtifactsAndClasspath(List<Artifact> artifacts, List<File> theClasspathFiles) {
        artifacts.addAll(project.getRuntimeArtifacts());
        theClasspathFiles.add(new File(project.getBuild().getOutputDirectory()));
    }
}
