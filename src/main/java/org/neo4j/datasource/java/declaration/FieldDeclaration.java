package org.neo4j.datasource.java.declaration;

public interface FieldDeclaration {
    int getAccess();

    String getName();

    String getType();

    String getSignature();
}
