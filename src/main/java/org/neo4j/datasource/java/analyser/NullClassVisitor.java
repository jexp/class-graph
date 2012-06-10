package org.neo4j.datasource.java.analyser;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class NullClassVisitor extends ClassAdapter implements RecursingClassVisitor<Void> {
    private static final ClassWriter NULL_WRITER = new ClassWriter(0);

    public NullClassVisitor() {
        super(NULL_WRITER);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        return null;
    }

    @Override public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    public Void get() {
        return null;
    }
}