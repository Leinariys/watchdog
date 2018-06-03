/*
 * Copyright (c) 2018, Kasra Faghihi, All rights reserved.
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

import com.offbynull.watchdog.instrumenter.InstrumentationSettings;
import com.offbynull.watchdog.instrumenter.Instrumenter;
import com.offbynull.watchdog.instrumenter.PluginHelper;
import com.offbynull.watchdog.instrumenter.generators.DebugGenerators.MarkerType;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Abstract instrumentation mojo. Provides base functionality for instrumentation.
 * @author Kasra Faghihi
 */
public abstract class AbstractInstrumentMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    
    @Parameter(property = "watchdog.markerType", defaultValue = "NONE")
    private MarkerType markerType;

    /**
     * Instruments all classes in a path recursively.
     * @param log maven logger
     * @param classpath classpath for classes being instrumented
     * @param path directory containing files to instrument
     * @throws MojoExecutionException if any exception occurs
     */
    protected final void instrumentPath(Log log, List<String> classpath, File path)
            throws MojoExecutionException {
        try {
            Instrumenter instrumenter = getInstrumenter(log, classpath);
            InstrumentationSettings settings = new InstrumentationSettings(markerType);

            PluginHelper.instrument(instrumenter, settings, path, path, log::info);
        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to get compile classpath elements", ex);
        }
    }

    /**
     * Creates an {@link Instrumenter} instance.
     * @param log maven logger
     * @param classpath classpath for classes being instrumented
     * @return a new {@link Instrumenter}
     * @throws MojoExecutionException if any exception occurs
     */
    private Instrumenter getInstrumenter(Log log, List<String> classpath) throws MojoExecutionException {
        List<File> classpathFiles;
        try {
            log.debug("Getting compile classpath");
            classpathFiles = classpath
                    .stream().map(x -> new File(x)).collect(Collectors.toList());

            log.debug("Classpath for instrumentation is as follows: " + classpathFiles);
        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to get compile classpath elements", ex);
        }

        log.debug("Creating instrumenter...");

        try {
            return new Instrumenter(classpathFiles);
        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to create instrumenter", ex);
        }
    }

    /**
     * Gets the maven project details.
     * @return maven project
     */
    protected final MavenProject getProject() {
        return project;
    }

}
