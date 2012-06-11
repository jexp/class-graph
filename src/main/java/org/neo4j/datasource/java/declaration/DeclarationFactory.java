package org.neo4j.datasource.java.declaration;

import java.util.Collection;

public interface DeclarationFactory {
    ClassDeclaration createClassInfo(String name, int access);

    MethodDeclaration createMethodInfo(int access, String name, Collection<String> params, String returnType, Collection<String> exceptions);

    FieldDeclaration createFieldInfo(int access, String name, String typeName);
}
