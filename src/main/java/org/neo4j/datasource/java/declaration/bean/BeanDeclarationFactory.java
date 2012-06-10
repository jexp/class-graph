package org.neo4j.datasource.java.declaration.bean;

import org.neo4j.datasource.java.declaration.*;
import org.neo4j.datasource.java.declaration.DeclarationFactory;
import org.neo4j.datasource.java.declaration.FieldDeclaration;
import org.neo4j.datasource.java.declaration.MethodDeclaration;

import java.util.Collection;

public class BeanDeclarationFactory implements DeclarationFactory {
    private int id=2;

    public ClassDeclaration createClassInfo(final String name) {
        return new ClassDeclarationBean(name,id++);
    }

    public MethodDeclaration createMethodInfo(final int access, final String name, final Collection<String> params, final String returnType) {
        return new MethodDeclarationBean(access, name, returnType, params);
    }

    public FieldDeclaration createFieldInfo(final int access, final String name, final String typeName) {
        return new FieldDeclarationBean(access,name, typeName);
    }
}
