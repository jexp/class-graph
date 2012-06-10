package org.neo4j.datasource.java.declaration;

import java.util.Map;
import java.util.Collection;

public interface ClassDeclaration {
    int getId();
    
    String getName();

    Map<String, MethodDeclaration> getMethods();

    void addMethod(MethodDeclaration methodDeclaration);

    void addInterface(ClassDeclaration classDeclaration);

    void setSuperClass(ClassDeclaration classDeclaration);

    ClassDeclaration getSuperClass();

    Collection<ClassDeclaration> getAnInterfaces();

    void addField(FieldDeclaration fieldDeclaration);

    Map<String, FieldDeclaration> getFields();

    boolean isRoot();

    String getPackage();
    String getSimpleName();
}
