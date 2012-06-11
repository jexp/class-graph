package org.neo4j.datasource.java.analyser;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

public class ClassFileLocator {
    ClassLoader cl;
    // todo filesystem, jar files, mvn dependencies -> pom
    public ClassFileLocator(String path) {
        if (path == null) throw new IllegalArgumentException("Path must not be null");
        this.cl = new URLClassLoader(pathToUrls(path), ClassLoader.getSystemClassLoader());
    }

    private URL[] pathToUrls(final String path) {
        final String[] files = path.split(File.pathSeparator);
        URL[] urls = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            final String file = files[i];
            try {
                if (file.matches("^\\w://.+"))
                    urls[i]=new URL(file);
                else
                    urls[i]=new File(file).toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Error creating URL from "+ file,e);
            }
        }
        return urls;
    }

    public InputStream getStreamFromURL(final URL url) {
        try {
            if (url==null) {
                return null;
            }
            return new BufferedInputStream(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Error reading stream from url "+url,e);
        }
    }

    public URL resolveClassName(final String slashClassName) {
        final String fileClassName = slashClassName.concat(".class");
        return ClassLoader.getSystemResource(fileClassName);
    }
}
