/*
 * Copyright (c) 2017, Kasra Faghihi, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.offbynull.watchdog.mavenplugin;

import java.io.File;
import java.util.List;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Mojo to run watchdog instrumentation. Instruments test classes.
 * <p>
 * Sample usage in POM:
 * <pre>
 *     &lt;build&gt;
 *         &lt;plugins&gt;
 *             &lt;plugin&gt;
 *                 &lt;groupId&gt;com.offbynull.watchdog&lt;/groupId&gt;
 *                 &lt;artifactId&gt;maven-plugin&lt;/artifactId&gt;
 *                 &lt;version&gt;LATEST VERSION HERE&lt;/version&gt;
 *                 &lt;executions&gt;
 *                     &lt;execution&gt;
 *                         &lt;goals&gt;
 *                             &lt;goal&gt;test-instrument&lt;/goal&gt;
 *                         &lt;/goals&gt;
 *                     &lt;/execution&gt;
 *                 &lt;/executions&gt;
 *             &lt;/plugin&gt;
 *         &lt;/plugins&gt;
 *     &lt;/build&gt;
 * </pre>
 *
 * or directly call the goal instrument (e.g. mvn watchdog:test-instrument)
 *
 * @author Kasra Faghihi
 */
@Mojo(name = "test-instrument", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE)
public final class TestInstrumentMojo extends AbstractInstrumentMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();

        File testOutputFolder = new File(getProject().getBuild().getTestOutputDirectory());
        if (!testOutputFolder.isDirectory()) {
            log.warn("Test folder doesn't exist -- nothing to instrument");
            return;
        }
        
        List<String> classpath;
        try {
            classpath = getProject().getTestClasspathElements();
        } catch (DependencyResolutionRequiredException ex) {
            throw new MojoExecutionException("Dependency resolution problem", ex);
        }
        
        log.debug("Processing test output folder ... ");
        instrumentPath(log, classpath, testOutputFolder);
    }
}
