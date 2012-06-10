package org.neo4j.datasource.java.analyser;

import org.objectweb.asm.ClassVisitor;

public interface RecursingClassVisitor<R> extends ClassVisitor {
    R get();
}
