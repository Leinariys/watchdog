package com.offbynull.watchdog.mavenplugin;

import com.offbynull.watchdog.mavenplugin.TestInstrumentMojo;
import com.offbynull.watchdog.instrumenter.generators.DebugGenerators.MarkerType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public final class TestInstrumentMojoTest {
    
    private MavenProject mavenProject;
    
    private TestInstrumentMojo fixture;
    
    @Before
    public void setUp() throws Exception {
        fixture = new TestInstrumentMojo();
        
        mavenProject = Mockito.mock(MavenProject.class);
        Log log = Mockito.mock(Log.class);
        
        FieldUtils.writeField(fixture, "project", mavenProject, true);
        FieldUtils.writeField(fixture, "markerType", MarkerType.NONE, true);
        FieldUtils.writeField(fixture, "log", log, true);
    }

    @Test
    public void mustInstrumentClasses() throws Exception {
        byte[] classContent = readZipFromResource("TightLoopTest.zip").get("TightLoopTest.class");
        
        File testDir = null;
        try {
            // write out
            testDir = Files.createTempDirectory(getClass().getSimpleName()).toFile();
            File testClass = new File(testDir, "TightLoopTest.class");
            FileUtils.writeByteArrayToFile(testClass, classContent);
            
            // mock
            Mockito.when(mavenProject.getTestClasspathElements()).thenReturn(Collections.emptyList());
            Build build = Mockito.mock(Build.class);
            Mockito.when(mavenProject.getBuild()).thenReturn(build);
            Mockito.when(build.getTestOutputDirectory()).thenReturn(testDir.getAbsolutePath());
            
            // execute plugin
            fixture.execute();
            
            // read back in
            byte[] modifiedTestClassContent = FileUtils.readFileToByteArray(testClass);
            
            // test
            Assert.assertTrue(modifiedTestClassContent.length > classContent.length);
        } finally {
            if (testDir != null) {
                FileUtils.deleteDirectory(testDir);
            }
        }
    }
    
    @Test
    public void mustNotThrowExceptionWhenDirectoryDoesntExist() throws Exception {
        File testDir = null;
        try {
            // create a folder and delete it right away
            testDir = Files.createTempDirectory(getClass().getSimpleName()).toFile();
            File fakeFolder = new File(testDir, "DOESNOTEXIST");
            
            // mock
            Mockito.when(mavenProject.getTestClasspathElements()).thenReturn(Collections.emptyList());
            Build build = Mockito.mock(Build.class);
            Mockito.when(mavenProject.getBuild()).thenReturn(build);
            Mockito.when(build.getTestOutputDirectory()).thenReturn(fakeFolder.getAbsolutePath());
            
            // execute plugin
            fixture.execute();
        } finally {
            if (testDir != null) {
                FileUtils.deleteDirectory(testDir);
            }
        }
    }
    
    private Map<String, byte[]> readZipFromResource(String path) throws IOException {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL url = cl.getResource(path);
        Validate.isTrue(url != null);
        
        Map<String, byte[]> ret = new LinkedHashMap<>();
        
        try (InputStream is = url.openStream();
                ZipArchiveInputStream zais = new ZipArchiveInputStream(is)) {
            ZipArchiveEntry entry;
            while ((entry = zais.getNextZipEntry()) != null) {
                ret.put(entry.getName(), IOUtils.toByteArray(zais));
            }
        }
        
        return ret;
    }
    
}
