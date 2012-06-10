package org.neo4j.datasource.java;

import junit.framework.TestCase;
import org.junit.Test;
import org.neo4j.datasource.java.analyser.ClassInspector;
import org.neo4j.datasource.java.analyser.RecursingClassVisitor;
import org.neo4j.datasource.java.analyser.VirtualVisitor;

public class VirtualInspectorTest extends TestCase {
    @Test
    public void testInspectObject() {
        new ClassInspector<Void>(System.getProperty("java.class.path")) {
            protected RecursingClassVisitor<Void> createVisitor() {
                return VirtualVisitor.visitor(RecursingClassVisitor.class, this, 0);
            }
        }.inspectClass(Object.class);
    }

    @Test
    public void testInspectTest() {
        new ClassInspector<Void>(System.getProperty("java.class.path")) {
            protected RecursingClassVisitor<Void> createVisitor() {
                return VirtualVisitor.visitor(RecursingClassVisitor.class, this, 0);
            }
        }.inspectClass(getClass());
    }
}