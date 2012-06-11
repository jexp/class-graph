package org.neo4j.datasource.java.analyser;

import org.neo4j.datasource.java.declaration.ClassDeclaration;
import org.neo4j.datasource.java.declaration.DeclarationFactory;
import org.neo4j.datasource.java.declaration.FieldDeclaration;
import org.neo4j.datasource.java.declaration.MethodDeclaration;
import org.objectweb.asm.*;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class RecordingClassVisitor extends ClassAdapter implements RecursingClassVisitor<ClassDeclaration> {
    private ClassDeclaration classDeclaration;
    private final ClassInspector<ClassDeclaration> classInspector;
    private final DeclarationFactory declarationFactory;
    private static final ClassWriter NULL_WRITER = new ClassWriter(0);

    public RecordingClassVisitor(ClassInspector<ClassDeclaration> classInspector, final DeclarationFactory declarationFactory) {
        super(NULL_WRITER);
        this.classInspector = classInspector;
        this.declarationFactory = declarationFactory;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        if (classDeclaration == null) classDeclaration = declarationFactory.createClassInfo(name,access);
        if (superName != null) { // todo filter
            classDeclaration.setSuperClass(classInspector.inspectClass(superName));
        }
        if (interfaces != null) {
            for (String interfaceName : interfaces) {
                classDeclaration.addInterface(classInspector.inspectClass(interfaceName));
            }
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        final Collection<String> params = getParamTypes(desc);
        final String returnType = getReturnType(desc);
        List<String> exceptionList = exceptions == null ? Collections.<String>emptyList() : asList(exceptions);
        final MethodDeclaration methodDeclaration = declarationFactory.createMethodInfo(access, name, params, returnType, exceptionList);
        classDeclaration.addMethod(methodDeclaration);
        return null;
    }



    @Override public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        final String typeName = Type.getType(desc).getClassName();
        final FieldDeclaration fieldDeclaration = declarationFactory.createFieldInfo(access, name, typeName);
        classDeclaration.addField(fieldDeclaration);
        return null;
    }

    private Collection<String> getParamTypes(final String desc) {
        Collection<String> params=new ArrayList<String>();
        for (Type paramType : Type.getArgumentTypes(desc)) {
            params.add(paramType.getClassName());
        }
        return params;
    }

    private String getReturnType(final String desc) {
        final Type returnType = Type.getReturnType(desc);
        return returnType.getClassName();
    }

    public ClassDeclaration get() {
        return classDeclaration;
    }
}
