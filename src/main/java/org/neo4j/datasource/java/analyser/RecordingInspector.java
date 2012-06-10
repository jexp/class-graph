package org.neo4j.datasource.java.analyser;

import org.neo4j.datasource.java.declaration.ClassDeclaration;
import org.neo4j.datasource.java.declaration.DeclarationFactory;
import org.neo4j.datasource.java.declaration.bean.BeanDeclarationFactory;

import static org.neo4j.datasource.java.analyser.ClassInspectUtils.getClassPath;

public class RecordingInspector extends ClassInspector<ClassDeclaration> {
    private DeclarationFactory declarationFactory;

    public RecordingInspector() {
        this(getClassPath());
    }

    public RecordingInspector(final String classPath) {
        super(classPath);
        declarationFactory = new BeanDeclarationFactory();
    }

    public void setDeclarationFactory(final DeclarationFactory declarationFactory) {
        if (declarationFactory == null) throw new IllegalArgumentException("InfoFactory must not be null");
        this.declarationFactory = declarationFactory;
    }

    protected RecursingClassVisitor<ClassDeclaration> createVisitor() {
        return new RecordingClassVisitor(this, declarationFactory);
    }
}
