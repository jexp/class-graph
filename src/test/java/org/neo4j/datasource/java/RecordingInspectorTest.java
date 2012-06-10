package org.neo4j.datasource.java;

import static org.junit.Assert.*;
import org.junit.Test;
import org.neo4j.datasource.java.analyser.*;
import org.neo4j.datasource.java.declaration.ClassDeclaration;
import org.neo4j.datasource.util.SizeCountingOutputStream;

import javax.swing.text.JTextComponentBeanInfo;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

public class RecordingInspectorTest {
    @Test
    public void testIterateDtJar() {
        final ClassFileIterator fileIterator = new ClassFileIterator();
        final RecordingInspector inspector = new RecordingInspector();
        long count = 0;
        for (final String classFileName : fileIterator.getClassFileNames(fileIterator.getJarLocationByClass(JTextComponentBeanInfo.class))) {
            inspector.inspectClass(classFileName);
            count++;
        }
        assertEquals("Files in dt.jar", 43, count);
    }

    @Test
    public void testIterateClassesJar() throws IOException {
        final RecordingInspector inspector = new RecordingInspector();
        checkAndRunClassesJar(inspector);
        final Map<String, ClassDeclaration> classes = inspector.getClasses();
        final SizeCountingOutputStream countingStream = new SizeCountingOutputStream();
        new ObjectOutputStream(countingStream).writeObject(classes);
        System.out.println("countingStream = " + countingStream.getCount());
        assertTrue("20 MB of data", countingStream.getCount() > 20 * 1024 * 1024);
    }

    @Test
    public void testTimeClassesJar() throws IOException {
        final ClassInspector<Void> inspector = new ClassInspector<Void>(ClassInspectUtils.getClassPath()) {
            protected RecursingClassVisitor<Void> createVisitor() {
                return new NullClassVisitor();
            }
        };
        checkAndRunClassesJar(inspector);
    }

    private <T> void checkAndRunClassesJar(final ClassInspector<T> inspector) {
        final ClassFileIterator fileIterator = new ClassFileIterator();
        final String jarFileLocation = fileIterator.getJarLocationByClass(Object.class);
        long count = 0;
        long time = System.nanoTime();
        for (final String classFileName : fileIterator.getClassFileNames(jarFileLocation)) {
            final T result = inspector.inspectClass(classFileName);
            count++;
            if (count % 500 == 0) {
                long delta = (System.nanoTime() - time) / 1000 / 1000;
                System.out.println("count = " + count + " took " + delta + " ms.");
                time = System.nanoTime();
            }
        }
        assertEquals("Files in classes.jar", 20241, count);
    }
}
