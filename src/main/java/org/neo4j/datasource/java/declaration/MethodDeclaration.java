package org.neo4j.datasource.java.declaration;

import java.util.Collection;

public interface MethodDeclaration {
    String getName();

    String getReturnType();

    int getAccess();

    String getSignature();

    Collection<String> getParams();

    Collection<String> getExceptions();
}
