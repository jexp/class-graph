package org.neo4j.datasource.java;

import junit.framework.TestCase;
import org.junit.Test;
import org.neo4j.datasource.java.analyser.ClassFileIterator;
import org.neo4j.datasource.java.analyser.ClassInspectUtils;

public class ClassFileFinderTest extends TestCase {
    private ClassFileIterator classFileIterator;

    @Test
    public void testCreateNames() {
        checkFindInJar(classFileIterator.getJarLocationFromClassPath("dt.jar"), "javax/swing/text/JTextComponentBeanInfo");
    }
    @Test
    public void testFindObject() {
        final String objectClassFile = "java/lang/Object";
        checkFindInJar(classFileIterator.getJarLocationByClass(objectClassFile), objectClassFile);
    }
    @Test
    public void testFindJFrame() {
        final String objectClassFile = "javax/swing/JFrame";
        checkFindInJar(classFileIterator.getJarLocationByClass(objectClassFile), objectClassFile);
    }

    @Test
    public void testFindTestCase() {
        final String objectClassFile = ClassInspectUtils.toSlashName(getClass());
        checkFindInJar(classFileIterator.getJarLocationByClass(objectClassFile), objectClassFile);
    }

    private void checkFindInJar(final String jarPath, final String classFile) {
        System.out.printf("looking for %s in %s",classFile,jarPath);
        boolean classFound = false;
        for (String className : classFileIterator.getClassFileNames(jarPath)) {
            System.out.println("classNames = " + className);
            if (className.equals(classFile)) classFound = true;
        }
        assertTrue(classFile + " found in " + jarPath, classFound);
    }

    @Test
    public void testGetJarLocationByClass() {
        assertEquals("found in dt.jar", "/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/dt.jar", classFileIterator.getJarLocationByClass("javax/swing/text/JTextComponentBeanInfo"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        classFileIterator = new ClassFileIterator();
    }
}
