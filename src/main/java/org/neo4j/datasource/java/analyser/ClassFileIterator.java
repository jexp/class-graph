package org.neo4j.datasource.java.analyser;

import org.neo4j.data.file.FileHandler;
import org.neo4j.data.file.FileSystemVisitor;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.net.URL;
import java.net.MalformedURLException;

public class ClassFileIterator {

    public static final int CLASS_LENGTH = ".class".length();

    public void iterateJar(String jarLocation, ClassInspector<?> classInspector) {
        if (jarLocation == null) throw new IllegalArgumentException("JarLocation must not be null");
        for (String classFileName : getClassFileNames(jarLocation)) {
            classInspector.inspectClass(classFileName);
        }
    }

    public Iterable<String> getClassFileNames(final String classLocation) {
        if (classLocation.endsWith(".jar")) // todo zip
            return getClassesFromJarFile(classLocation);
        return getClassesFromDirectory(classLocation);
    }
    
    private Iterable<String> getClassesFromDirectory(final String classLocation) {
        final File classesDirectory = new File(classLocation);
        if (!classesDirectory.isDirectory()) throw new IllegalArgumentException(classLocation + " is no directory");
        final ClassFileCollector classFileCollector = new ClassFileCollector(classesDirectory);
        new FileSystemVisitor(classFileCollector, classesDirectory);
        return classFileCollector.getClassFiles(); 
    }

    private Iterable<String> getClassesFromJarFile(final String classLocation) {
        try {
            final JarFile jarFile = new JarFile(classLocation);
            final Enumeration<JarEntry> entries = jarFile.entries();
            Collection<String> classNames = new LinkedList<String>();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (!jarEntry.isDirectory()) {
                    final String classFileName = toClassFileName(jarEntry.getName());
                    if (classFileName != null)
                        classNames.add(classFileName);
                } else {

                }
            }
            return classNames;
        } catch (IOException e) {
            throw new RuntimeException("Error accessing jar file " + classLocation, e);
        }
    }

    private String toClassFileName(final String name) {
        assert name != null;
        final String fileName = new File(name).getPath();
        if (fileName.endsWith(".class"))
            return fileName.substring(0, fileName.length() - ".class".length());
        return null;
    }

    public String getJarLocationFromClassPath(final String jarName, final String classPath) {
        final String[] entries = classPath.split(File.pathSeparator);
        for (String entry : entries) {
            if (entry.endsWith(jarName)) return entry;
        }
        return null;
    }

    public String getJarLocationFromClassPath(final String jarName) {
        return getJarLocationFromClassPath(jarName, System.getProperty("java.class.path"));
    }

    public String getJarLocationByClass(final String className) {
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        final String classFileName = ClassInspectUtils.toSlashName(className) + ".class";
        final URL url = classLoader.getResource(classFileName);
        final String path = getPathFromUrl(url);
        if (path.endsWith("/" + classFileName)) {
            final int baseLength = path.length() - classFileName.length() - 1;
            final String basePath = path.substring(0, baseLength);
            if (basePath.endsWith("!")) return basePath.substring(0, basePath.length() - 1);
            return basePath;
        }
        return path;
    }

    public String getPathFromUrl(final URL url) {
        final String path = url.getPath();
        if (!path.startsWith("file:")) {
            return path;
        } else {
            try {
                return URLDecoder.decode(new URL(path).getPath());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Error parsing url " + path, e);
            }
        }
    }

    public String getJarLocationByClass(final Class<?> className) {
        if (className == null) throw new IllegalArgumentException("ClassName must not be null");
        return getJarLocationByClass(className.getName());
    }

    private static class ClassFileCollector extends FileHandler {
        public Collection<String> classFiles = new LinkedList<String>();
        private final String basePath;

        public ClassFileCollector(final File classesDirectory) {
            basePath = classesDirectory.getPath();
        }

        @Override public void handle(final File file) {
            if (!file.isDirectory() && file.getName().endsWith(".class")) {
                String filePath = file.getPath();
                if (filePath.startsWith(basePath)) filePath = filePath.substring(basePath.length()+1);
                int length = filePath.length();
                classFiles.add(filePath.substring(0,length - CLASS_LENGTH));
            }
        }

        public Collection<String> getClassFiles() {
            return classFiles;
        }
    }
}
