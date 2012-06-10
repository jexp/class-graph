package org.neo4j.datasource.java.declaration;

import java.util.Collection;

public interface DeclarationFactory {
    ClassDeclaration createClassInfo(String name);

    MethodDeclaration createMethodInfo(int access, String name, Collection<String> params, String returnType);

    FieldDeclaration createFieldInfo(int access, String name, String typeName);
}
