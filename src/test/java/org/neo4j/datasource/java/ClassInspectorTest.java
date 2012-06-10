package org.neo4j.datasource.java;

import junit.framework.TestCase;
import org.junit.Test;
import org.neo4j.datasource.java.analyser.RecordingInspector;
import org.neo4j.datasource.java.declaration.ClassDeclaration;

public class ClassInspectorTest extends TestCase {
    @Test
    public void testInspectObject() {
        final ClassDeclaration classDeclaration = new RecordingInspector().inspectClass(Object.class);
        assertEquals("isObject","java.lang.String", classDeclaration.getName());
        System.out.println("classInfo.getMethods() = " + classDeclaration.getMethods().values());
        System.out.println("classInfo.getFields() = " + classDeclaration.getFields().values());
        assertTrue("concat", classDeclaration.getMethods().containsKey("java.lang.String concat(java.lang.String)"));
        assertEquals("java.lang.Object", classDeclaration.getSuperClass().getName());
    }
}
